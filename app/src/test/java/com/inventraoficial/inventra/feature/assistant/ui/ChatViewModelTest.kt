package com.inventraoficial.inventra.feature.assistant.ui

import com.inventraoficial.inventra.data.remote.api.AcaoPendenteDto
import com.inventraoficial.inventra.data.remote.api.EventoIa
import com.inventraoficial.inventra.data.remote.api.MensagemHistoricoDto
import com.inventraoficial.inventra.data.remote.api.PassoHistoricoDto
import com.inventraoficial.inventra.data.remote.api.SessaoResumoDto
import com.inventraoficial.inventra.data.repository.AnexoParaEnvio
import com.inventraoficial.inventra.data.repository.ChatApiException
import com.inventraoficial.inventra.data.repository.FakeChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val fakeRepository = FakeChatRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun criarViewModel(store: ChatSessionStore = ChatSessionStore()) =
        ChatViewModel(chatRepository = fakeRepository, store = store)

    private fun aguardar() = dispatcher.scheduler.advanceUntilIdle()

    /** Faz [vezes] perguntas, esperando a resposta da IA de cada uma. */
    private fun conversar(
        viewModel: ChatViewModel,
        vezes: Int,
    ) {
        repeat(vezes) { i ->
            viewModel.onInputChange("pergunta ${i + 1}")
            viewModel.onSend()
            aguardar()
        }
    }

    @Test
    fun `estado inicial tem saudacao, conversa vazia e input vazio`() {
        val viewModel = criarViewModel()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.greeting.isNotBlank())
        assertTrue(uiState.mensagens.isEmpty())
        assertEquals("", uiState.inputValue)
    }

    @Test
    fun `onInputChange atualiza o campo de texto`() {
        val viewModel = criarViewModel()

        viewModel.onInputChange("Qual o estoque de leite?")

        assertEquals("Qual o estoque de leite?", viewModel.uiState.value.inputValue)
    }

    @Test
    fun `onSend limpa o campo e envia a pergunta para a IA`() {
        val viewModel = criarViewModel()
        viewModel.onInputChange("pergunta qualquer")

        viewModel.onSend()
        aguardar()

        assertEquals("", viewModel.uiState.value.inputValue)
        assertEquals(listOf("pergunta qualquer"), fakeRepository.perguntasRecebidas)
    }

    @Test
    fun `onSend com campo vazio nao envia nada`() {
        val viewModel = criarViewModel()
        viewModel.onInputChange("   ")

        viewModel.onSend()
        aguardar()

        assertTrue(fakeRepository.perguntasRecebidas.isEmpty())
        assertTrue(viewModel.uiState.value.mensagens.isEmpty())
    }

    @Test
    fun `resposta da IA entra na conversa`() {
        val viewModel = criarViewModel()
        viewModel.onInputChange("oi")

        viewModel.onSend()
        aguardar()

        val mensagens = viewModel.uiState.value.mensagens
        assertEquals(2, mensagens.size)
        assertTrue(mensagens[0].ehDoUsuario)
        assertEquals("oi", mensagens[0].conteudo)
        assertEquals(false, mensagens[1].ehDoUsuario)
        assertEquals("resposta da IA", mensagens[1].conteudo)
        assertEquals(false, viewModel.uiState.value.carregando)
    }

    @Test
    fun `falha ao falar com a IA mostra mensagem de erro`() {
        fakeRepository.erroParaLancar = RuntimeException("falhou")
        val viewModel = criarViewModel()
        viewModel.onInputChange("oi")

        viewModel.onSend()
        aguardar()

        val ultima = viewModel.uiState.value.mensagens.last()
        assertEquals(false, ultima.ehDoUsuario)
        assertEquals("Não consegui falar com a IA: falhou", ultima.conteudo)
        assertNotNull(viewModel.uiState.value.erro)
    }

    @Test
    fun `audio gravado entra na conversa com a transcricao e a resposta`() {
        val viewModel = criarViewModel()
        val audio = File("audio_teste.m4a")

        viewModel.onAudioGravado(audio)
        aguardar()

        val mensagens = viewModel.uiState.value.mensagens
        assertEquals(listOf(audio), fakeRepository.audiosRecebidos)
        assertEquals(2, mensagens.size)
        assertTrue(mensagens[0].ehDoUsuario)
        assertEquals("pergunta falada", mensagens[0].conteudo)
        assertEquals("resposta da IA", mensagens[1].conteudo)
    }

    @Test
    fun `audio sem transcricao mantem o texto de mensagem de voz sem icone de microfone`() {
        fakeRepository.transcricaoPadrao = null
        val viewModel = criarViewModel()

        viewModel.onAudioGravado(File("audio_teste.m4a"))
        aguardar()

        assertEquals("Mensagem de voz", viewModel.uiState.value.mensagens.first().conteudo)
    }

    @Test
    fun `audio que falha marca o balao como nao enviado`() {
        fakeRepository.erroParaLancar = java.io.IOException("sem conexao")
        val viewModel = criarViewModel()

        viewModel.onAudioGravado(File("audio_teste.m4a"))
        aguardar()

        assertEquals("Áudio não enviado", viewModel.uiState.value.mensagens.first().conteudo)
    }

    @Test
    fun `falha ao enviar audio mostra mensagem de erro`() {
        fakeRepository.erroParaLancar = RuntimeException("falhou")
        val viewModel = criarViewModel()

        viewModel.onAudioGravado(File("audio_teste.m4a"))
        aguardar()

        val ultima = viewModel.uiState.value.mensagens.last()
        assertEquals("Não consegui falar com a IA: falhou", ultima.conteudo)
        assertEquals(false, viewModel.uiState.value.carregando)
    }

    /** Escolhe os arquivos no "+" (ficam no campo, pendentes) e espera a tela assentar. */
    private fun anexar(
        viewModel: ChatViewModel,
        vararg nomes: String,
    ) {
        viewModel.onAnexosEscolhidos(nomes.map { anexo(it) })
        aguardar()
    }

    @Test
    fun `escolher arquivos so anexa ao campo e nao envia nada`() {
        val viewModel = criarViewModel()

        anexar(viewModel, "estoque.xlsx")

        val estado = viewModel.uiState.value
        assertEquals(listOf("estoque.xlsx"), estado.anexosPendentes.map { it.nome })
        assertTrue(estado.mensagens.isEmpty())
        assertTrue(fakeRepository.lotesDeAnexos.isEmpty())
        assertTrue(fakeRepository.perguntasRecebidas.isEmpty())
    }

    @Test
    fun `pergunta com arquivo sobe o arquivo e depois manda a pergunta`() {
        val viewModel = criarViewModel()
        anexar(viewModel, "estoque.xlsx")

        viewModel.onInputChange("insere esse excel na cozinha")
        viewModel.onSend()
        aguardar()

        val estado = viewModel.uiState.value
        assertEquals(listOf(1), fakeRepository.lotesDeAnexos.map { it.size })
        assertEquals(
            "insere esse excel na cozinha\n\n[Arquivos que anexei a esta mensagem: estoque.xlsx]",
            fakeRepository.perguntasRecebidas.single(),
        )
        assertEquals(2, estado.mensagens.size)
        assertTrue(estado.mensagens[0].ehDoUsuario)
        assertEquals("insere esse excel na cozinha\n\n📎 estoque.xlsx", estado.mensagens[0].conteudo)
        assertFalse(estado.mensagens[1].ehDoUsuario)
        assertTrue(estado.anexosPendentes.isEmpty())
        assertEquals("", estado.inputValue)
        assertFalse(estado.carregando)
        assertEquals(1, estado.respostasDaIa)
    }

    @Test
    fun `arquivo recusado aparece com o motivo antes da resposta da IA`() {
        fakeRepository.nomesRecusados = setOf("virus.exe")
        val viewModel = criarViewModel()
        anexar(viewModel, "contrato.pdf", "virus.exe")

        viewModel.onInputChange("resume o contrato")
        viewModel.onSend()
        aguardar()

        val mensagens = viewModel.uiState.value.mensagens
        assertEquals(3, mensagens.size)
        assertTrue(mensagens[1].conteudo.contains("**Não consegui guardar:**"))
        assertTrue(mensagens[1].conteudo.contains("- virus.exe: Arquivos executáveis ou de script não são aceitos."))
        assertFalse(mensagens[1].conteudo.contains("Guardei no IAI"))
        assertEquals(
            "resume o contrato\n\n[Arquivos que anexei a esta mensagem: contrato.pdf]",
            fakeRepository.perguntasRecebidas.single(),
        )
    }

    @Test
    fun `se a IA recusar todos os arquivos a pergunta nao e enviada`() {
        fakeRepository.nomesRecusados = setOf("virus.exe")
        val viewModel = criarViewModel()
        anexar(viewModel, "virus.exe")

        viewModel.onInputChange("le isso")
        viewModel.onSend()
        aguardar()

        assertTrue(fakeRepository.perguntasRecebidas.isEmpty())
        val ultima = viewModel.uiState.value.mensagens.last().conteudo
        assertTrue(ultima.contains("- virus.exe: Arquivos executáveis ou de script não são aceitos."))
        assertFalse(viewModel.uiState.value.carregando)
    }

    @Test
    fun `so arquivos sem pergunta mostram o que a IA guardou`() {
        val viewModel = criarViewModel()
        anexar(viewModel, "contrato.pdf", "estoque.xlsx")

        viewModel.onSend()
        aguardar()

        val mensagens = viewModel.uiState.value.mensagens
        assertEquals(2, mensagens.size)
        assertEquals("Enviei 2 arquivos: contrato.pdf, estoque.xlsx", mensagens[0].conteudo)
        assertTrue(mensagens[1].conteudo.contains("**Guardei no IAI:**"))
        assertTrue(mensagens[1].conteudo.contains("- contrato.pdf"))
        assertTrue(mensagens[1].conteudo.contains("Já pode me perguntar sobre eles."))
        assertTrue(fakeRepository.perguntasRecebidas.isEmpty())
        assertTrue(viewModel.uiState.value.anexosPendentes.isEmpty())
    }

    @Test
    fun `arquivos de uma pasta mostram so o nome do arquivo e vao em lotes de tres`() {
        val viewModel = criarViewModel()
        viewModel.onAnexosEscolhidos((1..7).map { anexo("Cardapios/verao/prato$it.pdf") })

        viewModel.onSend()
        aguardar()

        assertEquals(listOf(3, 3, 1), fakeRepository.lotesDeAnexos.map { it.size })
        val mensagens = viewModel.uiState.value.mensagens
        assertEquals("Enviei 7 arquivos: prato1.pdf, prato2.pdf, prato3.pdf e mais 4", mensagens[0].conteudo)
        assertTrue(mensagens[1].conteudo.contains("- prato7.pdf"))
    }

    @Test
    fun `falha de rede no envio lista os arquivos como nao guardados e para de enviar`() {
        fakeRepository.erroParaLancar = java.io.IOException("sem conexao")
        val viewModel = criarViewModel()
        viewModel.onAnexosEscolhidos((1..5).map { anexo("a$it.pdf") })

        viewModel.onSend()
        aguardar()

        assertEquals(1, fakeRepository.lotesDeAnexos.size)
        val resposta = viewModel.uiState.value.mensagens.last().conteudo
        assertTrue(resposta.contains("**Não consegui guardar:**"))
        assertTrue(resposta.contains("- a1.pdf: Não consegui falar com a IA: sem conexao"))
        assertTrue(resposta.contains("- a5.pdf: não enviado"))
        assertFalse(resposta.contains("Guardei no IAI"))
        assertFalse(viewModel.uiState.value.carregando)
        assertNotNull(viewModel.uiState.value.erro)
    }

    @Test
    fun `erro da IA com mensagem propria aparece no resumo dos arquivos`() {
        fakeRepository.erroParaLancar =
            ChatApiException(503, "Recurso de documentos indisponível: Qdrant não configurado.")
        val viewModel = criarViewModel()
        anexar(viewModel, "contrato.pdf")

        viewModel.onSend()
        aguardar()

        val resposta = viewModel.uiState.value.mensagens.last().conteudo
        assertTrue(resposta.contains("- contrato.pdf: Recurso de documentos indisponível: Qdrant não configurado."))
    }

    @Test
    fun `arquivos que o app descartou viram aviso e nao entram no campo`() {
        val viewModel = criarViewModel()

        viewModel.onAnexosEscolhidos(emptyList(), listOf("grande.pdf: passa de 10 MB."))
        aguardar()

        val estado = viewModel.uiState.value
        assertTrue(estado.anexosPendentes.isEmpty())
        assertTrue(estado.mensagens.isEmpty())
        assertTrue(estado.aviso.orEmpty().contains("grande.pdf: passa de 10 MB."))
    }

    @Test
    fun `passar do limite de arquivos no campo avisa e descarta o excedente`() {
        val viewModel = criarViewModel()

        viewModel.onAnexosEscolhidos((1..MAXIMO_DE_ANEXOS_POR_ESCOLHA).map { anexo("a$it.pdf") })
        viewModel.onAnexosEscolhidos(listOf(anexo("extra.pdf")))
        aguardar()

        val estado = viewModel.uiState.value
        assertEquals(MAXIMO_DE_ANEXOS_POR_ESCOLHA, estado.anexosPendentes.size)
        assertFalse(estado.anexosPendentes.any { it.nome == "extra.pdf" })
        assertTrue(estado.aviso.orEmpty().contains("extra.pdf"))
    }

    @Test
    fun `remover um arquivo do campo apaga o arquivo do cache`() {
        val temporario = File.createTempFile("anexo", ".pdf")
        val viewModel = criarViewModel()
        val anexo = AnexoParaEnvio(temporario, "anexo.pdf")
        viewModel.onAnexosEscolhidos(listOf(anexo))

        viewModel.onAnexoRemovido(anexo)
        aguardar()

        assertTrue(viewModel.uiState.value.anexosPendentes.isEmpty())
        assertFalse(temporario.exists())
    }

    @Test
    fun `arquivos do cache sao apagados depois do envio e nao contam como resposta da IA`() {
        val temporario = File.createTempFile("anexo", ".pdf")
        val viewModel = criarViewModel()
        viewModel.onAnexosEscolhidos(listOf(AnexoParaEnvio(temporario, "anexo.pdf")))

        viewModel.onSend()
        aguardar()

        assertFalse(temporario.exists())
        assertEquals(0, viewModel.uiState.value.respostasDaIa)
    }

    @Test
    fun `nova conversa descarta os arquivos que estavam no campo`() {
        val temporario = File.createTempFile("anexo", ".pdf")
        val viewModel = criarViewModel()
        viewModel.onAnexosEscolhidos(listOf(AnexoParaEnvio(temporario, "anexo.pdf")))

        viewModel.onNovaSessao()
        aguardar()

        assertTrue(viewModel.uiState.value.anexosPendentes.isEmpty())
        assertFalse(temporario.exists())
    }

    @Test
    fun `nao anexa arquivos enquanto le uma conversa antiga`() {
        val viewModel = prepararConversaAntigaAberta()

        viewModel.onAnexosEscolhidos(listOf(anexo("contrato.pdf")))
        aguardar()

        assertTrue(viewModel.uiState.value.anexosPendentes.isEmpty())
        assertTrue(fakeRepository.lotesDeAnexos.isEmpty())
    }

    private fun anexo(nome: String) = AnexoParaEnvio(File(nome.substringAfterLast('/')), nome)

    @Test
    fun `erro da IA com mensagem propria aparece sem prefixo`() {
        fakeRepository.erroParaLancar = ChatApiException(422, "Não consegui entender o áudio. Tente de novo.")
        val viewModel = criarViewModel()

        viewModel.onAudioGravado(File("audio_teste.m4a"))
        aguardar()

        val ultima = viewModel.uiState.value.mensagens.last()
        assertEquals("Não consegui entender o áudio. Tente de novo.", ultima.conteudo)
    }

    @Test
    fun `nova sessao encerra a anterior, limpa a conversa e gera novo id`() {
        val viewModel = criarViewModel()
        val sessaoAnterior = viewModel.uiState.value.sessionId
        viewModel.onInputChange("oi")
        viewModel.onSend()
        aguardar()

        viewModel.onNovaSessao()
        aguardar()

        assertEquals(listOf(sessaoAnterior), fakeRepository.sessoesEncerradas)
        assertTrue(viewModel.uiState.value.mensagens.isEmpty())
        assertNotEquals(sessaoAnterior, viewModel.uiState.value.sessionId)
    }

    @Test
    fun `nova conversa limpa a tela mesmo quando o encerramento falha e tenta de novo depois`() {
        val viewModel = criarViewModel()
        val sessaoAnterior = viewModel.uiState.value.sessionId
        viewModel.onInputChange("oi")
        viewModel.onSend()
        aguardar()
        fakeRepository.erroAoEncerrar = RuntimeException("timeout")

        viewModel.onNovaSessao()
        aguardar()

        assertTrue(viewModel.uiState.value.mensagens.isEmpty())
        assertNull(viewModel.uiState.value.erro)
        assertTrue(fakeRepository.sessoesEncerradas.isEmpty())

        fakeRepository.erroAoEncerrar = null
        viewModel.carregarHistorico()
        aguardar()

        assertEquals(listOf(sessaoAnterior), fakeRepository.sessoesEncerradas)
    }

    @Test
    fun `nova conversa limpa na hora, antes de o servidor responder`() {
        val viewModel = criarViewModel()
        viewModel.onInputChange("oi")
        viewModel.onSend()
        aguardar()

        viewModel.onNovaSessao()

        // Sem avançar o relógio do teste: o encerramento ainda não rodou, e a tela já está limpa.
        assertTrue(viewModel.uiState.value.mensagens.isEmpty())
        assertFalse(viewModel.uiState.value.carregando)
    }

    // ---------- início ----------

    @Test
    fun `voltar ao inicio so sai da tela e a conversa continua guardada`() {
        val viewModel = criarViewModel()
        val sessao = viewModel.uiState.value.sessionId
        conversar(viewModel, 1)
        var foiParaOInicio = false

        viewModel.onVoltarAoInicio { foiParaOInicio = true }
        aguardar()

        assertTrue(foiParaOInicio)
        assertTrue(fakeRepository.sessoesEncerradas.isEmpty())
        assertEquals(2, viewModel.uiState.value.mensagens.size)
        assertEquals(sessao, viewModel.uiState.value.sessionId)
    }

    @Test
    fun `voltar ao inicio no meio de uma resposta nao a perde`() {
        val store = ChatSessionStore()
        val viewModel = criarViewModel(store)
        viewModel.onInputChange("quanto tenho de arroz?")
        viewModel.onSend()

        viewModel.onVoltarAoInicio {}
        aguardar()

        val ultima = criarViewModel(store).uiState.value.mensagens.last()
        assertFalse(ultima.ehDoUsuario)
        assertEquals("resposta da IA", ultima.conteudo)
    }

    @Test
    fun `voltar ao inicio olhando uma conversa antiga devolve a conversa ao vivo`() {
        val viewModel = criarViewModel()
        conversar(viewModel, 1)
        fakeRepository.mensagensPorConversa = mapOf("c1" to listOf(MensagemHistoricoDto("human", "antiga")))
        viewModel.onConversaClick(ConversaResumo("c1", "Antiga"))
        aguardar()
        assertEquals("c1", viewModel.uiState.value.conversaVisualizadaId)

        viewModel.onVoltarAoInicio {}

        assertNull(viewModel.uiState.value.conversaVisualizadaId)
        assertEquals(2, viewModel.uiState.value.mensagens.size)
    }

    // ---------- saudação ----------

    @Test
    fun `saudacao usa o primeiro nome do usuario`() {
        fakeRepository.nomeDoUsuario = "Maria da Silva"

        val viewModel = criarViewModel()
        aguardar()

        assertEquals("Olá, Maria!", viewModel.uiState.value.greeting)
    }

    // ---------- histórico completo ----------

    @Test
    fun `conversa antiga reabre com passos, pensamento e grafico`() {
        fakeRepository.mensagensPorConversa =
            mapOf(
                "c1" to
                    listOf(
                        MensagemHistoricoDto("human", "quanto tenho?"),
                        MensagemHistoricoDto(
                            role = "iai",
                            content = "Você tem 30 kg.",
                            passos = listOf(PassoHistoricoDto("t1", "Estoque consultado", true)),
                            pensamento = "Somar os lotes.",
                        ),
                    ),
            )
        val viewModel = criarViewModel()

        viewModel.onConversaClick(ConversaResumo("c1", "Estoque"))
        aguardar()

        val resposta = viewModel.uiState.value.mensagens.last()
        assertEquals("Somar os lotes.", resposta.pensamento)
        assertEquals(listOf(PassoIa("t1", "Estoque consultado", true)), resposta.passos)
    }

    @Test
    fun `titulo da conversa vem da IA e cai no resumo quando falta`() {
        fakeRepository.sessoesDoHistorico =
            listOf(
                SessaoResumoDto("1", "2026-10-30T10:00:00", "O usuário perguntou sobre arroz. Depois...", "Estoque de arroz"),
                SessaoResumoDto("2", "2026-10-29T10:00:00", "O usuário pediu uma baixa. Depois...", null),
            )

        val viewModel = criarViewModel()
        aguardar()

        assertEquals(listOf("Estoque de arroz", "Pediu uma baixa"), viewModel.uiState.value.conversas.map { it.titulo })
    }

    // ---------- persistência ----------

    @Test
    fun `a conversa continua de onde parou quando o chat e aberto de novo`() {
        val store = ChatSessionStore()
        val primeiro = criarViewModel(store)
        primeiro.onInputChange("rascunho")
        conversar(primeiro, 1)
        primeiro.onInputChange("rascunho")

        val segundo = criarViewModel(store)

        assertEquals(2, segundo.uiState.value.mensagens.size)
        assertEquals("rascunho", segundo.uiState.value.inputValue)
        assertEquals(primeiro.uiState.value.sessionId, segundo.uiState.value.sessionId)
    }

    @Test
    fun `resposta que ja apareceu nao e digitada de novo ao voltar`() {
        val store = ChatSessionStore()
        val primeiro = criarViewModel(store)
        conversar(primeiro, 1)
        assertNotNull(primeiro.uiState.value.mensagemAnimandoId)

        val segundo = criarViewModel(store)

        assertNull(segundo.uiState.value.mensagemAnimandoId)
    }

    @Test
    fun `conversa guardada de outro usuario e descartada`() {
        val store = ChatSessionStore()
        store.estado.value = ChatUiState(mensagens = listOf(Mensagem("1", "segredo", true)))
        store.donoId = "outro-usuario"

        criarViewModel(store)
        aguardar()

        assertTrue(store.estado.value.mensagens.isEmpty())
        assertEquals("usuario-teste", store.donoId)
    }

    // ---------- digitação ----------

    @Test
    fun `resposta nova fica marcada para digitar ate a tela avisar que terminou`() {
        val viewModel = criarViewModel()

        conversar(viewModel, 1)

        val idDaResposta = viewModel.uiState.value.mensagens.last().id
        assertEquals(idDaResposta, viewModel.uiState.value.mensagemAnimandoId)

        viewModel.onDigitacaoTerminou(idDaResposta)

        assertNull(viewModel.uiState.value.mensagemAnimandoId)
    }

    // ---------- histórico ----------

    private fun sessaoDoHistorico(
        id: String,
        resumo: String,
    ) = SessaoResumoDto(doc_id = id, started_at = "2026-10-22T14:05:11.123000", summary = resumo)

    @Test
    fun `historico carrega as conversas encerradas com titulo e data`() {
        fakeRepository.sessoesDoHistorico =
            listOf(sessaoDoHistorico("doc1", "O usuário perguntou sobre lotes vencendo."))
        val viewModel = criarViewModel()

        aguardar()

        val conversas = viewModel.uiState.value.conversas
        assertEquals(1, conversas.size)
        assertEquals("doc1", conversas[0].id)
        assertEquals("Perguntou sobre lotes vencendo", conversas[0].titulo)
        assertTrue(conversas[0].iniciadaEmMillis > 0)
    }

    @Test
    fun `falha ao carregar o historico mantem a lista que ja estava na tela`() {
        fakeRepository.sessoesDoHistorico = listOf(sessaoDoHistorico("doc1", "Resumo."))
        val viewModel = criarViewModel()
        aguardar()
        fakeRepository.erroNoHistorico = RuntimeException("sem rede")

        viewModel.carregarHistorico()
        aguardar()

        assertEquals(1, viewModel.uiState.value.conversas.size)
        assertFalse(viewModel.uiState.value.historicoCarregando)
    }

    @Test
    fun `encerrar uma conversa atualiza o historico`() {
        val viewModel = criarViewModel()
        conversar(viewModel, 1)
        val consultasAntes = fakeRepository.consultasDeHistorico
        fakeRepository.sessoesDoHistorico = listOf(sessaoDoHistorico("doc9", "Resumo novo."))

        viewModel.onNovaSessao()
        aguardar()

        assertTrue(fakeRepository.consultasDeHistorico > consultasAntes)
        assertEquals("doc9", viewModel.uiState.value.conversas.single().id)
    }

    private fun prepararConversaAntigaAberta(): ChatViewModel {
        fakeRepository.sessoesDoHistorico = listOf(sessaoDoHistorico("doc1", "Resumo antigo."))
        fakeRepository.mensagensPorConversa =
            mapOf(
                "doc1" to
                    listOf(
                        MensagemHistoricoDto(role = "human", content = "pergunta antiga"),
                        MensagemHistoricoDto(role = "iai", content = "resposta antiga"),
                    ),
            )
        val viewModel = criarViewModel()
        aguardar()
        conversar(viewModel, 1)
        viewModel.onConversaClick(viewModel.uiState.value.conversas.single())
        aguardar()
        return viewModel
    }

    @Test
    fun `abrir conversa do historico mostra as mensagens em modo leitura e guarda a conversa ao vivo`() {
        val viewModel = prepararConversaAntigaAberta()

        val estado = viewModel.uiState.value
        assertEquals("doc1", estado.conversaVisualizadaId)
        assertEquals(listOf("pergunta antiga", "resposta antiga"), estado.mensagens.map { it.conteudo })
        assertEquals(listOf(true, false), estado.mensagens.map { it.ehDoUsuario })
        assertEquals(2, estado.conversaEmAndamento?.mensagens?.size)
        assertNull(estado.abrindoConversaId)
    }

    @Test
    fun `nao da para enviar mensagem enquanto le uma conversa antiga`() {
        val viewModel = prepararConversaAntigaAberta()
        val perguntasAntes = fakeRepository.perguntasRecebidas.size

        viewModel.onInputChange("quero continuar")
        viewModel.onSend()
        aguardar()

        assertEquals(perguntasAntes, fakeRepository.perguntasRecebidas.size)
    }

    @Test
    fun `voltar para a conversa atual restaura as mensagens ao vivo`() {
        val viewModel = prepararConversaAntigaAberta()

        viewModel.onVoltarParaConversaAtual()

        val estado = viewModel.uiState.value
        assertNull(estado.conversaVisualizadaId)
        assertEquals(listOf("pergunta 1", "resposta da IA"), estado.mensagens.map { it.conteudo })
        assertEquals(1, estado.conversas.size)
        assertNull(estado.conversaEmAndamento)
    }

    @Test
    fun `nova conversa durante a leitura encerra a conversa ao vivo`() {
        val viewModel = prepararConversaAntigaAberta()
        val sessaoAoVivo = viewModel.uiState.value.conversaEmAndamento!!.sessionId

        viewModel.onNovaSessao()
        aguardar()

        assertEquals(listOf(sessaoAoVivo), fakeRepository.sessoesEncerradas)
        val estado = viewModel.uiState.value
        assertNull(estado.conversaVisualizadaId)
        assertTrue(estado.mensagens.isEmpty())
    }

    @Test
    fun `falha ao abrir conversa do historico avisa e mantem a tela`() {
        fakeRepository.sessoesDoHistorico = listOf(sessaoDoHistorico("doc1", "Resumo antigo."))
        fakeRepository.erroAoAbrirConversa = RuntimeException("sem rede")
        val viewModel = criarViewModel()
        aguardar()

        viewModel.onConversaClick(viewModel.uiState.value.conversas.single())
        aguardar()

        val estado = viewModel.uiState.value
        assertNotNull(estado.aviso)
        assertNull(estado.conversaVisualizadaId)
        assertNull(estado.abrindoConversaId)

        viewModel.onAvisoMostrado()
        assertNull(viewModel.uiState.value.aviso)
    }

    // ---------- ações propostas pela IA e artefatos ----------

    private fun acaoDto(id: String = "acao-1") =
        AcaoPendenteDto(
            id = id,
            proposal_hash = "hash-$id",
            operation = "registrar_saida",
            parameters = JsonObject(mapOf("batchId" to JsonPrimitive(1), "quantity" to JsonPrimitive("10"))),
            justification = "uso",
            status = "AGUARDANDO_APROVACAO",
            expires_at = "2099-01-01T00:00:00Z",
        )

    /** Conversa em que a IA propôs uma ação; o ViewModel volta com o cartão já na tela. */
    private fun conversaComAcaoProposta(): ChatViewModel {
        fakeRepository.acoesDaResposta = listOf(acaoDto())
        val viewModel = criarViewModel()
        viewModel.onInputChange("dê baixa em 10 kg de arroz")
        viewModel.onSend()
        aguardar()
        return viewModel
    }

    private fun ChatViewModel.acao(): AcaoPendente =
        uiState.value.mensagens
            .last()
            .acoes
            .first()

    @Test
    fun `acao proposta pela IA aparece como cartao aguardando e o texto perde o estado antigo`() {
        fakeRepository.respostaPadrao =
            "**Registrar saída** — proposta `a`\n- Estado: AGUARDANDO_APROVACAO — aguardando"

        val viewModel = conversaComAcaoProposta()

        val resposta = viewModel.uiState.value.mensagens.last()
        assertFalse(resposta.ehDoUsuario)
        assertEquals(1, resposta.acoes.size)
        assertEquals(EstadoDaAcao.AGUARDANDO, viewModel.acao().estado)
        assertFalse(resposta.conteudo.contains("Estado:"))
        assertFalse(resposta.conteudo.contains("proposta `a`"))
        assertTrue(resposta.conteudo.startsWith("Preparei uma proposta para você revisar."))
        assertTrue(fakeRepository.decisoesRecebidas.isEmpty())
    }

    @Test
    fun `aprovar manda a decisao com o hash da proposta e mostra a conclusao`() {
        val viewModel = conversaComAcaoProposta()

        viewModel.onAcaoAprovada("acao-1")
        aguardar()

        assertEquals(listOf(Triple("acao-1", "hash-acao-1", true)), fakeRepository.decisoesRecebidas)
        assertEquals(EstadoDaAcao.CONCLUIDA, viewModel.acao().estado)
        assertNull(viewModel.acao().detalhe)
    }

    @Test
    fun `recusar manda a recusa e o cartao fica recusado`() {
        val viewModel = conversaComAcaoProposta()

        viewModel.onAcaoRecusada("acao-1")
        aguardar()

        assertEquals(listOf(Triple("acao-1", "hash-acao-1", false)), fakeRepository.decisoesRecebidas)
        assertEquals(EstadoDaAcao.RECUSADA, viewModel.acao().estado)
        assertTrue(fakeRepository.execucoesRecebidas.isEmpty())
    }

    @Test
    fun `dois toques em aprovar decidem uma vez so`() {
        val viewModel = conversaComAcaoProposta()

        viewModel.onAcaoAprovada("acao-1")
        viewModel.onAcaoAprovada("acao-1")
        assertEquals(EstadoDaAcao.APROVANDO, viewModel.acao().estado)
        aguardar()

        assertEquals(1, fakeRepository.decisoesRecebidas.size)
    }

    @Test
    fun `falha de rede ao aprovar volta a aguardar com o motivo`() {
        val viewModel = conversaComAcaoProposta()
        fakeRepository.erroAoDecidir = IOException("sem rede")

        viewModel.onAcaoAprovada("acao-1")
        aguardar()

        assertEquals(EstadoDaAcao.AGUARDANDO, viewModel.acao().estado)
        assertTrue(viewModel.acao().detalhe.orEmpty().contains("sem rede"))
    }

    @Test
    fun `se o servidor ja decidiu antes da resposta se perder o cartao mostra o estado real`() {
        val viewModel = conversaComAcaoProposta()
        fakeRepository.erroAoDecidir = IOException("caiu na volta")
        fakeRepository.statusDaConsulta = "CONCLUIDA"

        viewModel.onAcaoAprovada("acao-1")
        aguardar()

        assertEquals(EstadoDaAcao.CONCLUIDA, viewModel.acao().estado)
    }

    @Test
    fun `recusa do servidor com texto proprio aparece no cartao`() {
        val viewModel = conversaComAcaoProposta()
        fakeRepository.erroAoDecidir = ChatApiException(409, "A decisão não corresponde à proposta imutável.")

        viewModel.onAcaoAprovada("acao-1")
        aguardar()

        assertEquals(EstadoDaAcao.AGUARDANDO, viewModel.acao().estado)
        assertEquals("A decisão não corresponde à proposta imutável.", viewModel.acao().detalhe)
    }

    @Test
    fun `aprovada mas nao executada oferece concluir a execucao`() {
        val viewModel = conversaComAcaoProposta()
        fakeRepository.statusAoAprovar = "APROVADA_PENDENTE_INTEGRACAO"
        viewModel.onAcaoAprovada("acao-1")
        aguardar()
        assertEquals(EstadoDaAcao.APROVADA, viewModel.acao().estado)

        viewModel.onAcaoExecutar("acao-1")
        aguardar()

        assertEquals(listOf("acao-1"), fakeRepository.execucoesRecebidas)
        assertEquals(EstadoDaAcao.CONCLUIDA, viewModel.acao().estado)
    }

    @Test
    fun `falha ao executar mantem a acao aprovada com o motivo`() {
        val viewModel = conversaComAcaoProposta()
        fakeRepository.statusAoAprovar = "APROVADA_PENDENTE_INTEGRACAO"
        viewModel.onAcaoAprovada("acao-1")
        aguardar()
        fakeRepository.erroAoExecutar = IOException("sem rede")
        fakeRepository.statusDaConsulta = "APROVADA_PENDENTE_INTEGRACAO"

        viewModel.onAcaoExecutar("acao-1")
        aguardar()

        assertEquals(EstadoDaAcao.APROVADA, viewModel.acao().estado)
        assertTrue(viewModel.acao().detalhe.orEmpty().contains("sem rede"))
    }

    @Test
    fun `executar so vale para acao ja aprovada`() {
        val viewModel = conversaComAcaoProposta()

        viewModel.onAcaoExecutar("acao-1")
        aguardar()

        assertTrue(fakeRepository.execucoesRecebidas.isEmpty())
        assertEquals(EstadoDaAcao.AGUARDANDO, viewModel.acao().estado)
    }

    @Test
    fun `acao que nao existe e ignorada`() {
        val viewModel = conversaComAcaoProposta()

        viewModel.onAcaoAprovada("nao-existe")
        aguardar()

        assertTrue(fakeRepository.decisoesRecebidas.isEmpty())
    }

    @Test
    fun `da para decidir uma acao com uma conversa do historico aberta`() {
        val viewModel = conversaComAcaoProposta()
        fakeRepository.sessoesDoHistorico =
            listOf(SessaoResumoDto(doc_id = "d1", started_at = "2026-01-01T10:00:00Z", summary = "Resumo."))
        fakeRepository.mensagensPorConversa =
            mapOf("d1" to listOf(MensagemHistoricoDto(role = "human", content = "oi")))
        viewModel.onConversaClick(ConversaResumo(id = "d1", titulo = "Resumo"))
        aguardar()
        assertEquals("d1", viewModel.uiState.value.conversaVisualizadaId)

        viewModel.onAcaoAprovada("acao-1")
        aguardar()
        viewModel.onVoltarParaConversaAtual()

        assertEquals(EstadoDaAcao.CONCLUIDA, viewModel.acao().estado)
    }

    @Test
    fun `tabela montada pela IA entra na resposta`() {
        fakeRepository.artefatosDaResposta =
            listOf(
                Json.parseToJsonElement(
                    """
                    {"type":"table","source":"spring","mocked":false,
                     "columns":[{"key":"produto","label":"Produto"},{"key":"quantidade","label":"Quantidade"}],
                     "rows":[{"produto":"Arroz","quantidade":12},{"produto":"Feijão","quantidade":3}]}
                    """.trimIndent(),
                ),
            )
        val viewModel = criarViewModel()
        viewModel.onInputChange("tabela do estoque")

        viewModel.onSend()
        aguardar()

        val tabela = viewModel.uiState.value.mensagens.last().artefato as Artefato.Tabela
        assertEquals(2, tabela.linhas.size)
        assertEquals(listOf("Arroz", "12"), tabela.linhas.first())
    }

    @Test
    fun `artefato que o app nao entende nao tira a resposta em texto`() {
        fakeRepository.artefatosDaResposta = listOf(Json.parseToJsonElement("""{"type":"mapa"}"""))
        val viewModel = criarViewModel()
        viewModel.onInputChange("mostre um mapa")

        viewModel.onSend()
        aguardar()

        val resposta = viewModel.uiState.value.mensagens.last()
        assertNull(resposta.artefato)
        assertEquals("resposta da IA", resposta.conteudo)
    }

    @Test
    fun `menu traz conversas que ficaram abertas mas esconde a que esta na tela`() {
        val viewModel = criarViewModel()
        val daTela = viewModel.uiState.value.sessionId
        fakeRepository.sessoesDoHistorico =
            listOf(
                SessaoResumoDto("d1", "2026-10-30T10:00:00", "", "Pergunta antiga", session_id = "outra", ativa = true),
                SessaoResumoDto("d2", "2026-10-31T10:00:00", "", "Conversa de agora", session_id = daTela, ativa = true),
                SessaoResumoDto("d3", "2026-10-29T10:00:00", "O usuário pediu arroz.", "Arroz", session_id = "encerrada"),
            )

        viewModel.carregarHistorico()
        aguardar()

        assertEquals(listOf("Pergunta antiga", "Arroz"), viewModel.uiState.value.conversas.map { it.titulo })
    }

    @Test
    fun `parar interrompe a resposta, libera o campo e deixa os passos sem girar`() {
        fakeRepository.eventosDoStream = listOf(EventoIa.Etapa("t1", concluida = false, texto = "Consultando o estoque…"))
        fakeRepository.travarDepoisDosEventos = true
        val viewModel = criarViewModel()
        viewModel.onInputChange("quais produtos tem?")
        viewModel.onSend()
        aguardar()
        assertTrue(viewModel.uiState.value.carregando)
        assertNotNull(viewModel.uiState.value.iniciouEmMillis)

        viewModel.onPararResposta()

        val estado = viewModel.uiState.value
        assertFalse(estado.carregando)
        assertNull(estado.iniciouEmMillis)
        assertTrue(estado.mensagens.first { !it.ehDoUsuario && it.passos.isNotEmpty() }.passos.single().concluido)
        assertEquals("Você interrompeu a resposta.", estado.mensagens.last().conteudo)
    }

    @Test
    fun `parar antes de chegar qualquer coisa so avisa que interrompeu`() {
        fakeRepository.eventosDoStream = emptyList()
        fakeRepository.travarDepoisDosEventos = true
        val viewModel = criarViewModel()
        viewModel.onInputChange("oi")
        viewModel.onSend()
        aguardar()

        viewModel.onPararResposta()

        assertEquals(listOf("oi", "Você interrompeu a resposta."), viewModel.uiState.value.mensagens.map { it.conteudo })
        assertFalse(viewModel.uiState.value.carregando)
    }

    @Test
    fun `resposta que termina guarda quanto tempo levou e zera o contador`() {
        fakeRepository.eventosDoStream = listOf(EventoIa.Texto("Você tem 30 kg."), EventoIa.Fim(respostaSegura = false))
        val viewModel = criarViewModel()
        viewModel.onInputChange("quanto tenho de arroz?")
        viewModel.onSend()
        aguardar()

        val estado = viewModel.uiState.value
        assertNull(estado.iniciouEmMillis)
        assertNotNull(estado.mensagens.last().duracaoSegundos)
    }
}
