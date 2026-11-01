package com.inventraoficial.inventra.data.repository

import com.inventraoficial.inventra.data.remote.api.AprovacaoDto
import com.inventraoficial.inventra.data.remote.api.ChatApi
import com.inventraoficial.inventra.data.remote.api.ChatAudioResponse
import com.inventraoficial.inventra.data.remote.api.ChatRequest
import com.inventraoficial.inventra.data.remote.api.ChatResponse
import com.inventraoficial.inventra.data.remote.api.DecisaoRequest
import com.inventraoficial.inventra.data.remote.api.EncerrarSessaoRequest
import com.inventraoficial.inventra.data.remote.api.EncerrarSessaoResponse
import com.inventraoficial.inventra.data.remote.api.EnvioArquivosResponse
import com.inventraoficial.inventra.data.remote.api.EventoIa
import com.inventraoficial.inventra.data.remote.api.HistoricoResponse
import com.inventraoficial.inventra.data.remote.api.MensagensResponse
import com.inventraoficial.inventra.data.remote.api.UserApi
import com.inventraoficial.inventra.data.remote.api.eventosIa
import com.inventraoficial.inventra.data.remote.dto.user.UserResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

/** Erro devolvido pela IA já com texto próprio para o usuário (ex.: "Áudio acima de 10 MB."). */
class ChatApiException(
    val codigo: Int,
    mensagem: String,
) : Exception(mensagem)

/** Um arquivo pronto para subir. [nome] é o que a IA recebe (pode ter pasta: "cardapios/verao/prato.pdf"). */
data class AnexoParaEnvio(
    val arquivo: File,
    val nome: String,
)

interface ChatRepository {
    /** Id do usuário logado — a IA exige que o user_id seja o da identidade do token. */
    suspend fun obterUsuarioId(): String

    /** Nome do usuário logado, para a saudação; null quando não deu para saber. */
    suspend fun obterNomeUsuario(): String?

    suspend fun enviarMensagem(
        sessionId: String,
        pergunta: String,
        userId: String,
    ): ChatResponse

    /**
     * Mesma pergunta de [enviarMensagem], respondida aos poucos: etapas, pensamento e texto chegam
     * conforme a IA trabalha. Se o servidor ainda não tem /chat/stream, devolve a resposta inteira.
     */
    fun conversarStream(
        sessionId: String,
        pergunta: String,
        userId: String,
    ): Flow<EventoIa>

    /** Envia um áudio gravado para a IA transcrever e responder. */
    suspend fun enviarAudio(
        sessionId: String,
        userId: String,
        arquivo: File,
    ): ChatAudioResponse

    /** Envia arquivos para a IA guardar como contexto das próximas perguntas (POST /arquivos). */
    suspend fun enviarArquivos(anexos: List<AnexoParaEnvio>): EnvioArquivosResponse

    /**
     * Aprova ([aprovar] = true) ou recusa uma ação que a IA propôs. [propostaHash] é o da proposta
     * recebida no chat: se ela mudou, o servidor recusa. Aprovar já manda executar.
     */
    suspend fun decidirAcao(
        acaoId: String,
        propostaHash: String,
        aprovar: Boolean,
    ): AprovacaoDto

    /** Termina a execução de uma ação já aprovada (também serve para ver como ela ficou). */
    suspend fun executarAcao(acaoId: String): AprovacaoDto

    /** Estado atual de uma ação proposta. */
    suspend fun consultarAcao(acaoId: String): AprovacaoDto

    /** Retorna null quando a IA não tinha nada para encerrar (404: sessão sem mensagens). */
    suspend fun encerrarSessao(sessionId: String): EncerrarSessaoResponse?

    /** Conversas já encerradas do usuário (as mais recentes primeiro). */
    suspend fun listarHistorico(userId: String): HistoricoResponse

    /** Mensagens de uma conversa do histórico. */
    suspend fun mensagensDaSessao(docId: String): MensagensResponse
}

class NetworkChatRepository(
    private val chatApi: ChatApi,
    private val userApi: UserApi,
) : ChatRepository {
    private var usuario: UserResponse? = null

    private suspend fun usuarioLogado(): UserResponse = usuario ?: userApi.me().also { usuario = it }

    override suspend fun obterUsuarioId(): String = usuarioLogado().id

    override suspend fun obterNomeUsuario(): String? = usuarioLogado().name.takeIf { it.isNotBlank() }

    override suspend fun enviarMensagem(
        sessionId: String,
        pergunta: String,
        userId: String,
    ): ChatResponse =
        try {
            chatApi.enviarMensagem(
                ChatRequest(
                    session_id = sessionId,
                    pergunta = pergunta,
                    user_id = userId,
                ),
            )
        } catch (e: HttpException) {
            val detalhe = detalheDoErro(e)
            throw if (detalhe != null) ChatApiException(e.code(), detalhe) else e
        }

    override fun conversarStream(
        sessionId: String,
        pergunta: String,
        userId: String,
    ): Flow<EventoIa> =
        flow {
            val request = ChatRequest(session_id = sessionId, pergunta = pergunta, user_id = userId)
            val corpo =
                try {
                    chatApi.conversarStream(request)
                } catch (e: HttpException) {
                    if (e.code() == HTTP_NOT_FOUND || e.code() == HTTP_METHOD_NOT_ALLOWED) {
                        // Servidor sem streaming: a conversa segue pelo POST /chat de sempre.
                        emit(EventoIa.RespostaCompleta(enviarMensagem(sessionId, pergunta, userId)))
                        return@flow
                    }
                    val detalhe = detalheDoErro(e)
                    throw if (detalhe != null) ChatApiException(e.code(), detalhe) else e
                }
            emitAll(corpo.eventosIa(jsonDoStream))
        }

    override suspend fun enviarAudio(
        sessionId: String,
        userId: String,
        arquivo: File,
    ): ChatAudioResponse {
        val textoPuro = "text/plain".toMediaType()
        val parteAudio =
            MultipartBody.Part.createFormData(
                name = CAMPO_AUDIO,
                filename = arquivo.name,
                body = arquivo.asRequestBody("audio/mp4".toMediaType()),
            )
        return try {
            chatApi.enviarAudio(
                audio = parteAudio,
                sessionId = sessionId.toRequestBody(textoPuro),
                userId = userId.toRequestBody(textoPuro),
            )
        } catch (e: HttpException) {
            val detalhe = detalheDoErro(e)
            throw if (detalhe != null) ChatApiException(e.code(), detalhe) else e
        }
    }

    override suspend fun enviarArquivos(anexos: List<AnexoParaEnvio>): EnvioArquivosResponse {
        val partes =
            anexos.map { anexo ->
                MultipartBody.Part.createFormData(
                    name = CAMPO_ARQUIVOS,
                    filename = anexo.nome,
                    body = anexo.arquivo.asRequestBody(tipoDoArquivo(anexo.nome).toMediaType()),
                )
            }
        return try {
            chatApi.enviarArquivos(partes)
        } catch (e: HttpException) {
            val detalhe = detalheDoErro(e)
            throw if (detalhe != null) ChatApiException(e.code(), detalhe) else e
        }
    }

    override suspend fun decidirAcao(
        acaoId: String,
        propostaHash: String,
        aprovar: Boolean,
    ): AprovacaoDto =
        comDetalheDoErro {
            chatApi.decidirAcao(
                id = acaoId,
                request = DecisaoRequest(decision = if (aprovar) "approve" else "reject", proposal_hash = propostaHash),
            )
        }

    override suspend fun executarAcao(acaoId: String): AprovacaoDto =
        comDetalheDoErro { chatApi.executarAcao(acaoId) }

    override suspend fun consultarAcao(acaoId: String): AprovacaoDto =
        comDetalheDoErro { chatApi.detalheDaAcao(acaoId) }

    override suspend fun encerrarSessao(sessionId: String): EncerrarSessaoResponse? =
        try {
            chatApi.encerrarSessao(EncerrarSessaoRequest(session_id = sessionId))
        } catch (e: HttpException) {
            if (e.code() == HTTP_NOT_FOUND) null else throw e
        }

    override suspend fun listarHistorico(userId: String): HistoricoResponse =
        chatApi.historico(userId = userId, limite = LIMITE_DO_HISTORICO)

    override suspend fun mensagensDaSessao(docId: String): MensagensResponse = chatApi.mensagens(docId)

    private fun tipoDoArquivo(nome: String): String =
        when (nome.substringAfterLast('.', "").lowercase()) {
            "pdf" -> "application/pdf"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "csv" -> "text/csv"
            "txt", "md" -> "text/plain"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "webp" -> "image/webp"
            else -> "application/octet-stream"
        }

    /** Roda a chamada e, se a IA recusar com um "detail", devolve o erro já com o texto para o usuário. */
    private suspend fun <T> comDetalheDoErro(chamada: suspend () -> T): T =
        try {
            chamada()
        } catch (e: HttpException) {
            val detalhe = detalheDoErro(e)
            throw if (detalhe != null) ChatApiException(e.code(), detalhe) else e
        }

    /** Lê o campo "detail" que a IA devolve nos erros (415, 422, 413, 502...). */
    private fun detalheDoErro(e: HttpException): String? =
        runCatching {
            val corpo = e.response()?.errorBody()?.string().orEmpty()
            val detalhe = (Json.parseToJsonElement(corpo) as? JsonObject)?.get("detail") as? JsonPrimitive
            detalhe?.contentOrNull
        }.getOrNull()?.takeIf { it.isNotBlank() }

    companion object {
        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_METHOD_NOT_ALLOWED = 405

        // Eventos novos que o app ainda não conhece não podem derrubar a conversa.
        private val jsonDoStream = Json { ignoreUnknownKeys = true }

        // A IA aceita de 1 a 100 conversas por consulta.
        private const val LIMITE_DO_HISTORICO = 50

        // Nome do campo do arquivo no multipart de POST /chat/audio (confirmar no OpenAPI da IA).
        private const val CAMPO_AUDIO = "audio"

        // Nome do campo (repetido, um por arquivo) do multipart de POST /arquivos.
        private const val CAMPO_ARQUIVOS = "arquivos"
    }
}
