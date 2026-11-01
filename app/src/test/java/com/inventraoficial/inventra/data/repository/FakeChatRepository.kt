package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.AcaoPendenteDto
import com.inventraoficial.inventra.data.remote.api.AprovacaoDto
import com.inventraoficial.inventra.data.remote.api.ChatAudioResponse
import com.inventraoficial.inventra.data.remote.api.ChatResponse
import com.inventraoficial.inventra.data.remote.api.EncerrarSessaoResponse
import com.inventraoficial.inventra.data.remote.api.EnvioArquivosResponse
import com.inventraoficial.inventra.data.remote.api.EventoIa
import com.inventraoficial.inventra.data.remote.api.HistoricoResponse
import com.inventraoficial.inventra.data.remote.api.MensagemHistoricoDto
import com.inventraoficial.inventra.data.remote.api.MensagensResponse
import com.inventraoficial.inventra.data.remote.api.ResultadoArquivoDto
import com.inventraoficial.inventra.data.remote.api.ResultadoExecucaoDto
import com.inventraoficial.inventra.data.remote.api.SessaoResumoDto
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonElement
import java.io.File

class FakeChatRepository : ChatRepository {
    var usuarioId: String = "usuario-teste"
    var respostaPadrao: String = "resposta da IA"
    var transcricaoPadrao: String? = "pergunta falada"
    var erroParaLancar: Throwable? = null
    var erroAoEncerrar: Throwable? = null
    var erroNoHistorico: Throwable? = null
    var erroAoAbrirConversa: Throwable? = null
    var sessoesDoHistorico: List<SessaoResumoDto> = emptyList()
    var mensagensPorConversa: Map<String, List<MensagemHistoricoDto>> = emptyMap()
    val perguntasRecebidas: MutableList<String> = mutableListOf()
    val audiosRecebidos: MutableList<File> = mutableListOf()
    val sessoesEncerradas: MutableList<String> = mutableListOf()
    var consultasDeHistorico: Int = 0

    /** O que a IA devolve junto com o texto: tabela/gráfico (JSON bruto) e ações propostas. */
    var artefatosDaResposta: List<JsonElement> = emptyList()
    var acoesDaResposta: List<AcaoPendenteDto> = emptyList()

    /** Estado que o servidor devolve ao aprovar, executar ou consultar uma ação. */
    var statusAoAprovar: String = "CONCLUIDA"
    var statusAoExecutar: String = "CONCLUIDA"
    var statusDaConsulta: String = "AGUARDANDO_APROVACAO"
    var mensagemDoResultado: String = "Saída registrada."
    var erroAoDecidir: Throwable? = null
    var erroAoExecutar: Throwable? = null
    var erroAoConsultarAcao: Throwable? = null
    val decisoesRecebidas: MutableList<Triple<String, String, Boolean>> = mutableListOf()
    val execucoesRecebidas: MutableList<String> = mutableListOf()

    /** Nomes de arquivo que a IA "recusa" (os demais são indexados). */
    var nomesRecusados: Set<String> = emptySet()
    val lotesDeAnexos: MutableList<List<AnexoParaEnvio>> = mutableListOf()

    var nomeDoUsuario: String? = "Maria da Silva"

    override suspend fun obterUsuarioId(): String = usuarioId

    override suspend fun obterNomeUsuario(): String? = nomeDoUsuario

    override suspend fun enviarMensagem(
        sessionId: String,
        pergunta: String,
        userId: String,
    ): ChatResponse {
        perguntasRecebidas += pergunta
        erroParaLancar?.let { throw it }
        return ChatResponse(respostaPadrao, artefatosDaResposta, acoesDaResposta)
    }

    /**
     * Eventos que o stream entrega. Nulo = servidor sem streaming: a resposta de [enviarMensagem]
     * chega inteira (como no fallback para POST /chat).
     */
    var eventosDoStream: List<EventoIa>? = null

    /** O stream entrega [eventosDoStream] e fica esperando (resposta ainda em andamento), até ser cancelado. */
    var travarDepoisDosEventos: Boolean = false

    /** Erro que o stream lança depois de entregar [eventosDoStream] (conexão caindo no meio). */
    var erroNoMeioDoStream: Throwable? = null

    override fun conversarStream(
        sessionId: String,
        pergunta: String,
        userId: String,
    ): Flow<EventoIa> =
        flow {
            perguntasRecebidas += pergunta
            erroParaLancar?.let { throw it }
            val eventos = eventosDoStream
            if (eventos == null) {
                emit(EventoIa.RespostaCompleta(ChatResponse(respostaPadrao, artefatosDaResposta, acoesDaResposta)))
            } else {
                eventos.forEach { emit(it) }
                if (travarDepoisDosEventos) awaitCancellation()
                erroNoMeioDoStream?.let { throw it }
            }
        }

    override suspend fun enviarAudio(
        sessionId: String,
        userId: String,
        arquivo: File,
    ): ChatAudioResponse {
        audiosRecebidos += arquivo
        erroParaLancar?.let { throw it }
        return ChatAudioResponse(
            resposta = respostaPadrao,
            transcricao = transcricaoPadrao,
            artifacts = artefatosDaResposta,
            pending_actions = acoesDaResposta,
        )
    }

    override suspend fun enviarArquivos(anexos: List<AnexoParaEnvio>): EnvioArquivosResponse {
        lotesDeAnexos += anexos
        erroParaLancar?.let { throw it }
        val resultados =
            anexos.map { anexo ->
                if (anexo.nome in nomesRecusados) {
                    ResultadoArquivoDto(
                        nome = anexo.nome,
                        status = "recusado",
                        mensagem = "Arquivos executáveis ou de script não são aceitos.",
                        codigo = "executavel",
                    )
                } else {
                    ResultadoArquivoDto(nome = anexo.nome, status = "indexado", trechos = 1)
                }
            }
        val indexados = resultados.count { it.status == "indexado" }
        return EnvioArquivosResponse(
            resultados = resultados,
            indexados = indexados,
            recusados = resultados.size - indexados,
        )
    }

    override suspend fun decidirAcao(
        acaoId: String,
        propostaHash: String,
        aprovar: Boolean,
    ): AprovacaoDto {
        decisoesRecebidas += Triple(acaoId, propostaHash, aprovar)
        erroAoDecidir?.let { throw it }
        return aprovacao(acaoId, if (aprovar) statusAoAprovar else "RECUSADA")
    }

    override suspend fun executarAcao(acaoId: String): AprovacaoDto {
        execucoesRecebidas += acaoId
        erroAoExecutar?.let { throw it }
        return aprovacao(acaoId, statusAoExecutar)
    }

    override suspend fun consultarAcao(acaoId: String): AprovacaoDto {
        erroAoConsultarAcao?.let { throw it }
        return aprovacao(acaoId, statusDaConsulta)
    }

    private fun aprovacao(
        acaoId: String,
        status: String,
    ) = AprovacaoDto(
        id = acaoId,
        status = status,
        result =
            if (status == "CONCLUIDA") {
                ResultadoExecucaoDto(ok = true, code = "OK", message = mensagemDoResultado)
            } else {
                null
            },
    )

    override suspend fun encerrarSessao(sessionId: String): EncerrarSessaoResponse? {
        erroAoEncerrar?.let { throw it }
        sessoesEncerradas += sessionId
        return EncerrarSessaoResponse(resumo = "resumo")
    }

    override suspend fun listarHistorico(userId: String): HistoricoResponse {
        consultasDeHistorico++
        erroNoHistorico?.let { throw it }
        return HistoricoResponse(sessoes = sessoesDoHistorico)
    }

    override suspend fun mensagensDaSessao(docId: String): MensagensResponse {
        erroAoAbrirConversa?.let { throw it }
        return MensagensResponse(messages = mensagensPorConversa[docId].orEmpty())
    }
}
