package com.inventraoficial.inventra.feature.assistant.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.inventraoficial.inventra.InventraApplication
import com.inventraoficial.inventra.data.remote.api.AcaoPendenteDto
import com.inventraoficial.inventra.data.remote.api.ChatResponse
import com.inventraoficial.inventra.data.remote.api.EventoIa
import com.inventraoficial.inventra.data.remote.api.MensagemHistoricoDto
import com.inventraoficial.inventra.data.remote.api.ResultadoArquivoDto
import com.inventraoficial.inventra.data.remote.api.SessaoResumoDto
import com.inventraoficial.inventra.data.repository.AnexoParaEnvio
import com.inventraoficial.inventra.data.repository.ChatApiException
import com.inventraoficial.inventra.data.repository.ChatRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import java.io.File

/**
 * Regras do chat. O estado e as chamadas à IA vivem no [ChatSessionStore]: sair da tela não
 * apaga a conversa; ela só termina quando o usuário encerra (ou diz que o problema foi resolvido).
 */
class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val store: ChatSessionStore = ChatSessionStore(),
) : ViewModel() {
    val uiState: StateFlow<ChatUiState> = store.estado.asStateFlow()

    init {
        // Uma resposta que chegou com o usuário fora da tela já aparece pronta, sem "digitar" de novo.
        atualizar { it.copy(mensagemAnimandoId = null) }
        store.escopo.launch { prepararConversaDoUsuario() }
    }

    fun onInputChange(value: String) {
        atualizar { it.copy(inputValue = value) }
    }

    /**
     * Envia o que está no campo: a pergunta e/ou os arquivos anexados. Com arquivos, eles sobem
     * primeiro (a IA guarda o conteúdo) e só então a pergunta segue, para a IA já poder consultá-los.
     */
    fun onSend() {
        val atual = store.estado.value
        val pergunta = atual.inputValue.trim()
        val anexos = atual.anexosPendentes
        if ((pergunta.isEmpty() && anexos.isEmpty()) || atual.carregando || atual.conversaVisualizadaId != null) return

        atualizar { it.copy(inputValue = "", anexosPendentes = emptyList()) }
        when {
            anexos.isEmpty() -> enviarPergunta(pergunta)
            pergunta.isEmpty() -> guardarSoAnexos(anexos)
            else -> enviarPerguntaComAnexos(pergunta, anexos)
        }
    }

    private fun enviarPergunta(pergunta: String) {
        enviarEmStream(textoDoUsuario = pergunta) { PerguntaPronta(pergunta) }
    }

    private fun enviarPerguntaComAnexos(
        pergunta: String,
        anexos: List<AnexoParaEnvio>,
    ) {
        enviarEmStream(textoDoUsuario = textoDaPerguntaComAnexos(pergunta, anexos)) {
            val envio = subirAnexos(anexos)
            val guardados = envio.resultados.filter { it.status == STATUS_INDEXADO }
            if (guardados.isEmpty()) {
                throw AnexosNaoGuardados(resumoDosAnexos(envio.resultados, emptyList()))
            }
            val problemas = resumoDosAnexos(envio.resultados, emptyList(), soProblemas = true)
            PerguntaPronta(
                pergunta = perguntaParaIa(pergunta, guardados),
                avisoDosAnexos = problemas.takeIf { it.isNotBlank() },
            )
        }
    }

    /** Recebe o áudio gravado, manda para a IA e mostra a transcrição + resposta na conversa. */
    fun onAudioGravado(arquivo: File) {
        val atual = store.estado.value
        if (atual.carregando || atual.conversaVisualizadaId != null) {
            arquivo.delete()
            return
        }
        enviar(textoDoUsuario = TEXTO_TRANSCREVENDO, textoSeFalhar = TEXTO_AUDIO_NAO_ENVIADO) {
            try {
                val resposta =
                    chatRepository.enviarAudio(
                        sessionId = store.estado.value.sessionId,
                        userId = chatRepository.obterUsuarioId(),
                        arquivo = arquivo,
                    )
                montarResposta(
                    texto = resposta.resposta,
                    artefatos = resposta.artifacts,
                    acoes = resposta.pending_actions,
                    transcricao =
                        resposta.transcricao?.takeIf { it.isNotBlank() }
                            ?: TEXTO_MENSAGEM_DE_VOZ_SEM_TRANSCRICAO,
                )
            } finally {
                arquivo.delete()
            }
        }
    }

    /**
     * Arquivos escolhidos no "+" (fotos, documentos, pasta), já copiados para o cache: ficam anexados
     * ao campo de mensagem e só sobem quando o usuário envia. [ignorados] são os que o app já
     * descartou (grandes demais, ilegíveis...); o usuário é avisado.
     */
    fun onAnexosEscolhidos(
        anexos: List<AnexoParaEnvio>,
        ignorados: List<String> = emptyList(),
    ) {
        val atual = store.estado.value
        if (atual.carregando || atual.conversaVisualizadaId != null) {
            apagarAnexos(anexos)
            return
        }
        val vagas = (MAXIMO_DE_ANEXOS_POR_ESCOLHA - atual.anexosPendentes.size).coerceAtLeast(0)
        val aceitos = anexos.take(vagas)
        val demais = anexos.drop(vagas)
        apagarAnexos(demais)
        val naoAnexados =
            ignorados +
                demais.map {
                    "${it.nome.substringAfterLast('/')}: passa do limite de $MAXIMO_DE_ANEXOS_POR_ESCOLHA arquivos."
                }
        atualizar {
            it.copy(
                anexosPendentes = it.anexosPendentes + aceitos,
                aviso = if (naoAnexados.isEmpty()) it.aviso else "Não anexei:\n" + naoAnexados.joinToString("\n"),
            )
        }
    }

    /** O usuário tirou um arquivo do campo de mensagem antes de enviar. */
    fun onAnexoRemovido(anexo: AnexoParaEnvio) {
        atualizar { it.copy(anexosPendentes = it.anexosPendentes - anexo) }
        apagarAnexos(listOf(anexo))
    }

    /** Só arquivos, sem pergunta: sobem, e a IA responde no chat o que guardou. */
    private fun guardarSoAnexos(anexos: List<AnexoParaEnvio>) {
        val minhaMensagem = Mensagem(id = novoId("user"), conteudo = textoDosAnexos(anexos), ehDoUsuario = true)
        atualizar {
            it.copy(
                mensagens = it.mensagens + minhaMensagem,
                carregando = true,
                erro = null,
            )
        }

        store.escopo.launch {
            val envio = subirAnexos(anexos)
            val resumo = mensagemDaIa(resumoDosAnexos(envio.resultados, emptyList()))
            atualizar { s ->
                s.copy(
                    mensagens = s.mensagens + resumo,
                    carregando = false,
                    erro = envio.falha?.let { textoDoErro(it) },
                )
            }
        }
    }

    /** Sobe os arquivos em lotes; se um lote falhar, os seguintes não são enviados. Apaga o cache no fim. */
    private suspend fun subirAnexos(anexos: List<AnexoParaEnvio>): EnvioDosAnexos {
        val resultados = mutableListOf<ResultadoArquivoDto>()
        var falha: Throwable? = null
        try {
            for (lote in anexos.chunked(ARQUIVOS_POR_ENVIO)) {
                if (falha != null) {
                    resultados += lote.map { naoGuardado(it.nome, TEXTO_ANEXO_NAO_ENVIADO) }
                    continue
                }
                tentar { chatRepository.enviarArquivos(lote) }
                    .onSuccess { resposta -> resultados += resposta.resultados }
                    .onFailure { e ->
                        falha = e
                        resultados += lote.map { naoGuardado(it.nome, textoDoErro(e)) }
                    }
            }
        } finally {
            apagarAnexos(anexos)
        }
        return EnvioDosAnexos(resultados, falha)
    }

    /**
     * Botão de parar (quadrado): cancela a resposta que está chegando. O app fecha a conexão, o servidor
     * para de gastar a IA, e o que já apareceu (passos, raciocínio) fica na tela, sem passos girando.
     */
    fun onPararResposta() {
        store.envioEmAndamento?.cancel()
        store.envioEmAndamento = null
        atualizar { s ->
            val ultima = s.mensagens.lastOrNull()
            val aviso = mensagemDaIa(TEXTO_RESPOSTA_PARADA)
            val mensagens =
                if (ultima != null && !ultima.ehDoUsuario) {
                    s.mensagens.dropLast(1) + ultima.copy(passos = ultima.passos.map { it.copy(concluido = true) }) + aviso
                } else {
                    s.mensagens + aviso
                }
            s.copy(mensagens = mensagens, carregando = false, iniciouEmMillis = null, mensagemAnimandoId = null)
        }
    }

    /**
     * Nova conversa: a tela limpa na hora e a conversa anterior é encerrada em segundo plano (a IA gera
     * o resumo e ela vai para o histórico). Se o encerramento falhar, ele é tentado de novo depois, sem
     * deixar a conversa velha na tela. Uma resposta que ainda chegava é cancelada.
     */
    fun onNovaSessao() {
        val ativa = store.estado.value.conversaAtiva()
        store.envioEmAndamento?.cancel()
        store.envioEmAndamento = null
        apagarAnexos(ativa.anexosPendentes)
        atualizar { it.novaConversaLimpa() }
        if (ativa.mensagens.isEmpty()) return

        store.escopo.launch {
            if (tentar { chatRepository.encerrarSessao(ativa.sessionId) }.isFailure) {
                store.sessoesParaEncerrar += ativa.sessionId
            }
            carregarHistoricoDoUsuario()
        }
    }

    /**
     * Seta de voltar do topo: só sai da tela. A conversa fica guardada (e uma resposta em andamento
     * continua chegando); ela só termina quando o usuário diz que o problema foi resolvido.
     * Se estava olhando uma conversa do histórico, volta para a conversa ao vivo antes de sair.
     */
    fun onVoltarAoInicio(irParaInicio: () -> Unit) {
        onVoltarParaConversaAtual()
        irParaInicio()
    }

    /** Atualiza a lista do menu lateral com as conversas já encerradas. */
    fun carregarHistorico() {
        store.escopo.launch { carregarHistoricoDoUsuario() }
    }

    /** Abre uma conversa do histórico em modo leitura, guardando a conversa ao vivo para voltar. */
    fun onConversaClick(conversa: ConversaResumo) {
        val atual = store.estado.value
        if (atual.carregando || atual.abrindoConversaId != null || atual.conversaVisualizadaId == conversa.id) return

        atualizar { it.copy(abrindoConversaId = conversa.id) }
        store.escopo.launch {
            tentar { chatRepository.mensagensDaSessao(conversa.id) }
                .onSuccess { resposta ->
                    atualizar { s ->
                        val aoVivo =
                            s.conversaEmAndamento
                                ?: s.copy(conversaEmAndamento = null, abrindoConversaId = null, aviso = null)
                        s.copy(
                            mensagens = resposta.messages.mapIndexed { i, m -> m.paraMensagem(conversa.id, i) },
                            inputValue = "",
                            mensagemAnimandoId = null,
                            conversaVisualizadaId = conversa.id,
                            abrindoConversaId = null,
                            conversaEmAndamento = aoVivo,
                            anexosPendentes = emptyList(),
                        )
                    }
                }.onFailure {
                    atualizar {
                        it.copy(abrindoConversaId = null, aviso = "Não consegui abrir essa conversa. Tente de novo.")
                    }
                }
        }
    }

    /** Sai do modo leitura e volta para a conversa que estava em andamento. */
    fun onVoltarParaConversaAtual() {
        atualizar { s ->
            if (s.conversaVisualizadaId == null) {
                s
            } else {
                (s.conversaEmAndamento ?: s.novaConversaLimpa()).copy(conversas = s.conversas)
            }
        }
    }

    /** O usuário tocou em "Aprovar" no cartão de uma ação que a IA propôs. */
    fun onAcaoAprovada(acaoId: String) = decidirAcao(acaoId, aprovar = true)

    /** O usuário tocou em "Recusar": a ação é descartada e nada muda no Inventra. */
    fun onAcaoRecusada(acaoId: String) = decidirAcao(acaoId, aprovar = false)

    private fun decidirAcao(
        acaoId: String,
        aprovar: Boolean,
    ) {
        val acao = store.estado.value.acaoPorId(acaoId) ?: return
        if (acao.estado != EstadoDaAcao.AGUARDANDO) return
        atualizarAcao(acaoId) {
            it.copy(estado = if (aprovar) EstadoDaAcao.APROVANDO else EstadoDaAcao.RECUSANDO, detalhe = null)
        }
        store.escopo.launch {
            tentar { chatRepository.decidirAcao(acaoId, acao.propostaHash, aprovar) }
                .onSuccess { resposta -> atualizarAcao(acaoId) { resposta.aplicarEm(it) } }
                .onFailure { e -> recuperarAcao(acaoId, e, voltarPara = EstadoDaAcao.AGUARDANDO) }
        }
    }

    /** A ação foi aprovada mas a execução não terminou: o usuário pede para concluir (ou ver como ficou). */
    fun onAcaoExecutar(acaoId: String) {
        val acao = store.estado.value.acaoPorId(acaoId) ?: return
        if (acao.estado != EstadoDaAcao.APROVADA) return
        atualizarAcao(acaoId) { it.copy(estado = EstadoDaAcao.EXECUTANDO, detalhe = null) }
        store.escopo.launch {
            tentar { chatRepository.executarAcao(acaoId) }
                .onSuccess { resposta -> atualizarAcao(acaoId) { resposta.aplicarEm(it) } }
                .onFailure { e -> recuperarAcao(acaoId, e, voltarPara = EstadoDaAcao.APROVADA) }
        }
    }

    /**
     * A chamada falhou, mas o servidor pode ter registrado a decisão antes de a resposta se perder.
     * Confere o estado real: se ele andou, o cartão passa a mostrá-lo; se não, volta ao estado anterior
     * com o motivo do erro, e o usuário pode tentar de novo.
     */
    private suspend fun recuperarAcao(
        acaoId: String,
        erro: Throwable,
        voltarPara: EstadoDaAcao,
    ) {
        val real = tentar { chatRepository.consultarAcao(acaoId) }.getOrNull()
        val estadoReal = real?.let { estadoDoServidor(it.status) }
        atualizarAcao(acaoId) { atual ->
            if (real != null && estadoReal != null && estadoReal != voltarPara) {
                real.aplicarEm(atual)
            } else {
                atual.copy(estado = voltarPara, detalhe = textoDoErro(erro))
            }
        }
    }

    private fun atualizarAcao(
        acaoId: String,
        bloco: (AcaoPendente) -> AcaoPendente,
    ) {
        atualizar { it.comAcaoAtualizada(acaoId, bloco) }
    }

    /** A tela terminou de "digitar" a resposta; só então a pergunta de resolução pode aparecer. */
    fun onDigitacaoTerminou(mensagemId: String) {
        atualizar { if (it.mensagemAnimandoId == mensagemId) it.copy(mensagemAnimandoId = null) else it }
    }

    fun onAvisoMostrado() {
        atualizar { it.copy(aviso = null) }
    }

    private fun atualizar(bloco: (ChatUiState) -> ChatUiState) {
        store.estado.update(bloco)
    }

    /** Descarta a conversa guardada se agora quem está logado é outro usuário; depois carrega o histórico. */
    private suspend fun prepararConversaDoUsuario() {
        val usuarioId = tentar { chatRepository.obterUsuarioId() }.getOrNull() ?: return
        val dono = store.donoId
        if (dono != null && dono != usuarioId) store.reiniciar()
        store.donoId = usuarioId
        val nome = tentar { chatRepository.obterNomeUsuario() }.getOrNull()
        atualizar { it.copy(greeting = saudacaoPara(nome)) }
        carregarHistoricoDoUsuario(usuarioId)
    }

    /** As conversas que o usuário está vendo (a da tela e a guardada atrás do histórico) não entram no menu. */
    private fun sessoesDaTela(estado: ChatUiState): Set<String> =
        setOfNotNull(estado.sessionId, estado.conversaEmAndamento?.sessionId)

    /** Encerra no servidor as conversas deixadas antes e que falharam na hora. */
    private suspend fun encerrarSessoesPendentes() {
        for (id in store.sessoesParaEncerrar.toList()) {
            if (tentar { chatRepository.encerrarSessao(id) }.isSuccess) store.sessoesParaEncerrar.remove(id)
        }
    }

    private suspend fun carregarHistoricoDoUsuario(usuarioId: String? = null) {
        encerrarSessoesPendentes()
        val id = usuarioId ?: tentar { chatRepository.obterUsuarioId() }.getOrNull() ?: return
        atualizar { it.copy(historicoCarregando = true) }
        val sessoes =
            tentar { chatRepository.listarHistorico(id).sessoes }.getOrNull()?.let { todas ->
                val daTela = sessoesDaTela(store.estado.value)
                todas.filter { it.session_id == null || it.session_id !in daTela }
            }
        atualizar { s ->
            s.copy(
                historicoCarregando = false,
                conversas = sessoes?.map { it.paraResumo() } ?: s.conversas,
            )
        }
    }

    private fun enviar(
        textoDoUsuario: String,
        textoSeFalhar: String? = null,
        chamada: suspend () -> RespostaIa,
    ) {
        val minhaMensagem =
            Mensagem(
                id = novoId("user"),
                conteudo = textoDoUsuario,
                ehDoUsuario = true,
            )
        atualizar {
            it.copy(
                mensagens = it.mensagens + minhaMensagem,
                carregando = true,
                erro = null,
                iniciouEmMillis = System.currentTimeMillis(),
            )
        }

        val envio = store.escopo.launch {
            tentar { chamada() }
                .onSuccess { resposta ->
                    val transcricao = resposta.transcricao?.takeIf { it.isNotBlank() }
                    val iaMensagem =
                        Mensagem(
                            id = novoId("ia"),
                            conteudo = resposta.resposta,
                            ehDoUsuario = false,
                            artefato = resposta.artefato,
                            acoes = resposta.acoes,
                        )
                    atualizar { s ->
                        val mensagensAtualizadas =
                            s.mensagens.map { mensagem ->
                                if (mensagem.id == minhaMensagem.id && transcricao != null) {
                                    mensagem.copy(conteudo = transcricao)
                                } else {
                                    mensagem
                                }
                            }
                        val respostas = s.respostasDaIa + 1
                        val aviso = resposta.avisoDosAnexos?.let { listOf(mensagemDaIa(it)) } ?: emptyList()
                        s.copy(
                            mensagens = mensagensAtualizadas + aviso + iaMensagem,
                            carregando = false,
                            iniciouEmMillis = null,
                            respostasDaIa = respostas,
                            mensagemAnimandoId = iaMensagem.id,
                        )
                    }
                }.onFailure { e -> registrarFalha(e, minhaMensagem.id, textoSeFalhar) }
        }
        store.envioEmAndamento = envio
        envio.invokeOnCompletion { if (store.envioEmAndamento === envio) store.envioEmAndamento = null }
    }

    /**
     * Envia a pergunta por POST /chat/stream e desenha a resposta conforme os eventos chegam:
     * passos e pensamento ao vivo, depois artefatos, cartões e o texto já conferido pela IA.
     * [preparar] roda antes (ex.: subir anexos) e devolve a pergunta que a IA recebe.
     */
    private fun enviarEmStream(
        textoDoUsuario: String,
        preparar: suspend () -> PerguntaPronta,
    ) {
        val minhaMensagem = Mensagem(id = novoId("user"), conteudo = textoDoUsuario, ehDoUsuario = true)
        atualizar {
            it.copy(
                mensagens = it.mensagens + minhaMensagem,
                carregando = true,
                erro = null,
                iniciouEmMillis = System.currentTimeMillis(),
            )
        }

        val envio = store.escopo.launch {
            val iaId = novoId("ia")
            val resposta = RespostaEmStream()
            var chegouCompleta = false
            tentar {
                val pronta = preparar()
                pronta.avisoDosAnexos?.let { aviso ->
                    atualizar { it.copy(mensagens = it.mensagens + mensagemDaIa(aviso)) }
                }
                chatRepository
                    .conversarStream(
                        sessionId = store.estado.value.sessionId,
                        pergunta = pronta.pergunta,
                        userId = chatRepository.obterUsuarioId(),
                    ).collect { evento ->
                        when (evento) {
                            is EventoIa.Erro -> {
                                throw ErroDoStream(evento.mensagem)
                            }

                            is EventoIa.RespostaCompleta -> {
                                chegouCompleta = true
                                mostrarRespostaCompleta(iaId, evento.resposta)
                            }

                            is EventoIa.Fim -> {
                                resposta.aplicar(evento)
                                if (resposta.temConteudo) publicar(iaId, resposta)
                                concluirResposta(iaId)
                            }

                            else -> if (resposta.aplicar(evento)) publicar(iaId, resposta)
                        }
                    }
                if (!resposta.terminou && !chegouCompleta) throw ErroDoStream(TEXTO_RESPOSTA_INTERROMPIDA)
            }.onFailure { e ->
                // Passos que ainda giravam não podem ficar girando depois da falha.
                if (resposta.temConteudo) {
                    resposta.encerrarPassosPendentes()
                    publicar(iaId, resposta)
                }
                registrarFalha(e, minhaMensagem.id, textoSeFalhar = null)
            }
        }
        store.envioEmAndamento = envio
        envio.invokeOnCompletion { if (store.envioEmAndamento === envio) store.envioEmAndamento = null }
    }

    /** Coloca (ou atualiza) a bolha da IA com o que já chegou. */
    private fun publicar(
        iaId: String,
        resposta: RespostaEmStream,
    ) {
        val mensagem = resposta.paraMensagem(iaId)
        atualizar { s ->
            val jaExiste = s.mensagens.any { it.id == iaId }
            s.copy(
                mensagens =
                    if (jaExiste) {
                        s.mensagens.map { if (it.id == iaId) mensagem else it }
                    } else {
                        s.mensagens + mensagem
                    },
            )
        }
    }

    /** Fim do stream: a resposta já foi vista aos poucos, então não "digita" de novo. */
    private fun concluirResposta(iaId: String) {
        atualizar { s ->
            val respostas = s.respostasDaIa + 1
            val duracao = s.iniciouEmMillis?.let { ((System.currentTimeMillis() - it) / MILIS_POR_SEGUNDO).toInt() }
            s.copy(
                mensagens = s.mensagens.map { if (it.id == iaId) it.copy(duracaoSegundos = duracao) else it },
                carregando = false,
                iniciouEmMillis = null,
                respostasDaIa = respostas,
                mensagemAnimandoId = null,
            )
        }
    }

    /** Servidor sem streaming: a resposta chega inteira e aparece "digitando", como antes. */
    private fun mostrarRespostaCompleta(
        iaId: String,
        resposta: ChatResponse,
    ) {
        val pronta = montarResposta(resposta.resposta, resposta.artifacts, resposta.pending_actions)
        val mensagem =
            Mensagem(
                id = iaId,
                conteudo = pronta.resposta,
                ehDoUsuario = false,
                artefato = pronta.artefato,
                acoes = pronta.acoes,
            )
        atualizar { s ->
            val respostas = s.respostasDaIa + 1
            s.copy(
                mensagens = s.mensagens + mensagem,
                carregando = false,
                iniciouEmMillis = null,
                respostasDaIa = respostas,
                mensagemAnimandoId = iaId,
            )
        }
    }

    private fun registrarFalha(
        e: Throwable,
        idDaMensagemDoUsuario: String,
        textoSeFalhar: String?,
    ) {
        val textoErro = textoDoErro(e)
        atualizar { s ->
            val mensagensAtuais =
                s.mensagens.map { mensagem ->
                    if (mensagem.id == idDaMensagemDoUsuario && textoSeFalhar != null) {
                        mensagem.copy(conteudo = textoSeFalhar)
                    } else {
                        mensagem
                    }
                }
            s.copy(
                mensagens = mensagensAtuais + mensagemDeErro(textoErro),
                carregando = false,
                iniciouEmMillis = null,
                erro = textoErro,
            )
        }
    }

    /** runCatching que não engole o cancelamento da coroutine. */
    private suspend fun <T> tentar(bloco: suspend () -> T): Result<T> =
        runCatching { bloco() }.onFailure { if (it is CancellationException) throw it }

    private fun textoDoErro(e: Throwable): String =
        if (e is AnexosNaoGuardados || e is ErroDoStream) {
            e.message.orEmpty()
        } else if (e is ChatApiException) {
            e.message ?: "Não consegui falar com a IA."
        } else {
            "Não consegui falar com a IA: ${e.message}"
        }

    /** Junta o que a IA devolveu: o texto, o artefato (se entendido) e os cartões das ações propostas. */
    private fun montarResposta(
        texto: String,
        artefatos: List<JsonElement>,
        acoes: List<AcaoPendenteDto>,
        transcricao: String? = null,
        avisoDosAnexos: String? = null,
    ): RespostaIa {
        val cartoes = acoes.map { it.paraAcao() }
        return RespostaIa(
            resposta = if (cartoes.isEmpty()) texto else textoComCartoes(texto, cartoes.size),
            transcricao = transcricao,
            avisoDosAnexos = avisoDosAnexos,
            artefato = lerArtefatos(artefatos).firstOrNull(),
            acoes = cartoes,
        )
    }

    private fun mensagemDaIa(texto: String) = Mensagem(id = novoId("anexo"), conteudo = texto, ehDoUsuario = false)

    private fun naoGuardado(
        nome: String,
        motivo: String,
    ) = ResultadoArquivoDto(nome = nome, status = STATUS_ERRO, mensagem = motivo)

    private fun apagarAnexos(anexos: List<AnexoParaEnvio>) {
        anexos.forEach { anexo ->
            anexo.arquivo.delete()
            // Cada anexo fica numa pasta própria dentro de cache/anexos; ela some junto com o arquivo.
            anexo.arquivo.parentFile?.takeIf { it.parentFile?.name == PASTA_DE_ANEXOS }?.delete()
        }
    }

    private fun textoDosAnexos(anexos: List<AnexoParaEnvio>): String {
        val nomes = anexos.map { it.nome.substringAfterLast('/') }
        return if (nomes.size == 1) {
            "Enviei o arquivo ${nomes.first()}"
        } else {
            val mostrados = nomes.take(NOMES_NO_BALAO).joinToString(", ")
            val demais = nomes.size - NOMES_NO_BALAO
            "Enviei ${nomes.size} arquivos: $mostrados" + if (demais > 0) " e mais $demais" else ""
        }
    }

    /** Balão do usuário: a pergunta e, embaixo, os arquivos que foram junto. */
    private fun textoDaPerguntaComAnexos(
        pergunta: String,
        anexos: List<AnexoParaEnvio>,
    ): String = pergunta + "\n\n" + anexos.joinToString("\n") { "📎 " + it.nome.substringAfterLast('/') }

    /** O que a IA recebe: a pergunta mais os nomes dos arquivos, para ela saber que deve consultá-los. */
    private fun perguntaParaIa(
        pergunta: String,
        guardados: List<ResultadoArquivoDto>,
    ): String = "$pergunta\n\n[Arquivos que anexei a esta mensagem: ${guardados.joinToString(", ") { it.nome }}]"

    /** Texto da resposta no chat: o que a IA guardou e o que não aceitou (com o motivo de cada um). */
    private fun resumoDosAnexos(
        resultados: List<ResultadoArquivoDto>,
        ignorados: List<String>,
        soProblemas: Boolean = false,
    ): String {
        val guardados = resultados.filter { it.status == STATUS_INDEXADO }
        val recusados = resultados.filter { it.status != STATUS_INDEXADO }
        val partes = mutableListOf<String>()
        if (guardados.isNotEmpty() && !soProblemas) {
            val linhas =
                guardados.joinToString("\n") { r ->
                    "- ${r.nome.substringAfterLast('/')}" + if (r.aviso.isNotBlank()) " (${r.aviso})" else ""
                }
            partes += "**Guardei no IAI:**\n$linhas"
        }
        if (recusados.isNotEmpty() || ignorados.isNotEmpty()) {
            val linhas =
                recusados.map { r ->
                    "- ${r.nome.substringAfterLast('/')}: ${r.mensagem.ifBlank { "não foi aceito." }}"
                } + ignorados.map { "- $it" }
            partes += "**Não consegui guardar:**\n${linhas.joinToString("\n")}"
        }
        if (guardados.isNotEmpty() && !soProblemas) {
            partes += "Já pode me perguntar sobre ${if (guardados.size == 1) "ele" else "eles"}."
        }
        return partes.joinToString("\n\n")
    }

    private fun mensagemDeErro(texto: String) =
        Mensagem(
            id = novoId("erro"),
            conteudo = texto,
            ehDoUsuario = false,
        )

    private fun SessaoResumoDto.paraResumo() =
        ConversaResumo(
            id = doc_id,
            titulo = titulo?.trim()?.takeIf { it.isNotEmpty() } ?: tituloDaConversa(summary),
            iniciadaEmMillis = interpretarDataIso(started_at)?.toEpochMilli() ?: 0L,
        )

    private fun MensagemHistoricoDto.paraMensagem(
        conversaId: String,
        posicao: Int,
    ) = Mensagem(
        id = "hist_${conversaId}_$posicao",
        conteudo = content,
        ehDoUsuario = role == PAPEL_DO_USUARIO,
        artefato = artifacts?.let { lerArtefatos(it) }?.firstOrNull(),
        passos = passos.orEmpty().map { PassoIa(it.id, it.texto, it.concluido, it.pai, it.detalhe) },
        pensamento = pensamento.orEmpty(),
    )

    private data class RespostaIa(
        val resposta: String,
        val transcricao: String? = null,
        /** Arquivos que a IA não aceitou, quando a pergunta veio com anexos. */
        val avisoDosAnexos: String? = null,
        /** Tabela ou gráfico que acompanha a resposta. */
        val artefato: Artefato? = null,
        /** Ações propostas que esperam aprovação do usuário. */
        val acoes: List<AcaoPendente> = emptyList(),
    )

    /** A pergunta que segue para a IA e, se algum anexo foi recusado, o aviso que aparece antes da resposta. */
    private data class PerguntaPronta(
        val pergunta: String,
        val avisoDosAnexos: String? = null,
    )

    /** A IA avisou (evento "erro") ou a conexão acabou antes do fim da resposta. */
    private class ErroDoStream(
        mensagem: String,
    ) : Exception(mensagem)

    private data class EnvioDosAnexos(
        val resultados: List<ResultadoArquivoDto>,
        val falha: Throwable?,
    )

    /** Nenhum anexo foi guardado; a mensagem já traz o motivo de cada um. */
    private class AnexosNaoGuardados(
        mensagem: String,
    ) : Exception(mensagem)

    companion object {
        private const val TEXTO_TRANSCREVENDO = "Transcrevendo…"
        private const val TEXTO_MENSAGEM_DE_VOZ_SEM_TRANSCRICAO = "Mensagem de voz"
        private const val TEXTO_AUDIO_NAO_ENVIADO = "Áudio não enviado"
        private const val TEXTO_RESPOSTA_PARADA = "Você interrompeu a resposta."
        private const val MILIS_POR_SEGUNDO = 1_000L
        private const val TEXTO_RESPOSTA_INTERROMPIDA = "A resposta foi interrompida antes de terminar. Tente de novo."
        private const val TEXTO_ANEXO_NAO_ENVIADO = "não enviado (o envio anterior falhou)."

        // Quantos arquivos vão por chamada: cada um passa pela checagem de segurança e pelos embeddings.
        private const val ARQUIVOS_POR_ENVIO = 3
        private const val NOMES_NO_BALAO = 3
        private const val STATUS_INDEXADO = "indexado"
        private const val STATUS_ERRO = "erro"
        private const val PASTA_DE_ANEXOS = "anexos"

        // Papel que a IA grava nas mensagens do usuário (as dela vêm como "iai").
        private const val PAPEL_DO_USUARIO = "human"

        val Factory: ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as InventraApplication
                    ChatViewModel(
                        chatRepository = application.container.chatRepository,
                        store = application.container.chatSessionStore,
                    )
                }
            }
    }
}
