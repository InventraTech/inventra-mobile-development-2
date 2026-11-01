package com.inventraoficial.inventra.data.remote.api

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

@Serializable
data class ChatRequest(
    val session_id: String,
    val pergunta: String,
    val user_id: String,
)

@Serializable
data class ChatResponse(
    val resposta: String,
    /** Tabela ou gráfico (no máximo um). Fica como JSON bruto e é lido com tolerância em [lerArtefatos]. */
    val artifacts: List<JsonElement> = emptyList(),
    /** Ações que a IA propôs e que só valem depois que o usuário aprova. */
    val pending_actions: List<AcaoPendenteDto> = emptyList(),
)

@Serializable
data class ChatAudioResponse(
    val resposta: String,
    val transcricao: String? = null,
    val artifacts: List<JsonElement> = emptyList(),
    val pending_actions: List<AcaoPendenteDto> = emptyList(),
)

/** Proposta de alteração (dar baixa, criar requisição...) à espera do "sim" do usuário. */
@Serializable
data class AcaoPendenteDto(
    val id: String,
    val proposal_hash: String,
    val operation: String = "",
    val parameters: JsonObject = JsonObject(emptyMap()),
    val justification: String = "",
    val status: String = "",
    val expires_at: String = "",
    /** Como estavam os registros afetados quando a proposta foi criada (`parameter` + `record`). */
    val before: List<JsonObject> = emptyList(),
)

@Serializable
data class DecisaoRequest(
    /** "approve" ou "reject". */
    val decision: String,
    val proposal_hash: String,
)

/** Estado atual de uma proposta (`status`: AGUARDANDO_APROVACAO, CONCLUIDA, RECUSADA, FALHOU...). */
@Serializable
data class AprovacaoDto(
    val id: String = "",
    val status: String = "",
    val result: ResultadoExecucaoDto? = null,
    val execution_error: String? = null,
)

@Serializable
data class ResultadoExecucaoDto(
    val ok: Boolean = false,
    val code: String = "",
    val message: String = "",
    val mocked: Boolean = false,
)

/** Resultado de um arquivo enviado para a IA guardar como contexto (`status`: indexado, recusado ou erro). */
@Serializable
data class ResultadoArquivoDto(
    val nome: String,
    val status: String,
    val mensagem: String = "",
    val codigo: String = "",
    val arquivo_id: String = "",
    val tipo: String = "",
    val trechos: Int = 0,
    val aviso: String = "",
)

@Serializable
data class EnvioArquivosResponse(
    val resultados: List<ResultadoArquivoDto> = emptyList(),
    val indexados: Int = 0,
    val recusados: Int = 0,
)

@Serializable
data class EncerrarSessaoRequest(
    val session_id: String,
)

@Serializable
data class EncerrarSessaoResponse(
    val resumo: String,
)

/** Uma conversa já encerrada: o resumo gerado pela IA e quando ela começou (ISO 8601). */
@Serializable
data class SessaoResumoDto(
    val doc_id: String,
    val started_at: String,
    val summary: String,
    /** Título curto do assunto, gerado pela IA ao encerrar; ausente em conversas antigas. */
    val titulo: String? = null,
    /** Id da sessão no app; o menu esconde a que está na tela. */
    val session_id: String? = null,
    /** Conversa que ainda não foi encerrada (ficou aberta); aparece no menu e abre em leitura. */
    val ativa: Boolean = false,
)

@Serializable
data class HistoricoResponse(
    val sessoes: List<SessaoResumoDto> = emptyList(),
)

@Serializable
data class MensagemHistoricoDto(
    val role: String,
    val content: String,
    /** O que a IA mostrou junto com a resposta: guardado para a conversa reabrir igual. */
    val passos: List<PassoHistoricoDto>? = null,
    val pensamento: String? = null,
    val artifacts: List<JsonElement>? = null,
)

@Serializable
data class PassoHistoricoDto(
    val id: String = "",
    val texto: String = "",
    val concluido: Boolean = true,
    val pai: String? = null,
    val detalhe: String? = null,
)

@Serializable
data class MensagensResponse(
    val messages: List<MensagemHistoricoDto> = emptyList(),
)

interface ChatApi {
    @POST("chat")
    suspend fun enviarMensagem(
        @Body request: ChatRequest,
    ): ChatResponse

    /**
     * Mesma conversa do [enviarMensagem], contada aos poucos (NDJSON). O @Streaming é obrigatório:
     * sem ele o Retrofit lê o corpo inteiro antes de devolver e o efeito some.
     */
    @Streaming
    @POST("chat/stream")
    suspend fun conversarStream(
        @Body request: ChatRequest,
    ): ResponseBody

    @Multipart
    @POST("chat/audio")
    suspend fun enviarAudio(
        @Part audio: MultipartBody.Part,
        @Part("session_id") sessionId: RequestBody,
        @Part("user_id") userId: RequestBody,
    ): ChatAudioResponse

    /** Envia arquivos (PDF, Word, Excel, PowerPoint, CSV, texto, imagens) para a IA usar como contexto. */
    @Multipart
    @POST("arquivos")
    suspend fun enviarArquivos(
        @Part arquivos: List<MultipartBody.Part>,
    ): EnvioArquivosResponse

    /** Aprova ou recusa uma proposta; aprovar já manda executar. Só quem pediu pode decidir. */
    @POST("approvals/{id}/decision")
    suspend fun decidirAcao(
        @Path("id") id: String,
        @Body request: DecisaoRequest,
    ): AprovacaoDto

    /** Termina a execução de uma proposta já aprovada (e serve para ver o andamento). */
    @POST("approvals/{id}/execute")
    suspend fun executarAcao(
        @Path("id") id: String,
    ): AprovacaoDto

    @GET("approvals/{id}")
    suspend fun detalheDaAcao(
        @Path("id") id: String,
    ): AprovacaoDto

    @POST("chat/encerrar")
    suspend fun encerrarSessao(
        @Body request: EncerrarSessaoRequest,
    ): EncerrarSessaoResponse

    /** Conversas encerradas do usuário, da mais recente para a mais antiga. */
    @GET("chat/historico/{user_id}")
    suspend fun historico(
        @Path("user_id") userId: String,
        @Query("limite") limite: Int,
    ): HistoricoResponse

    /** Mensagens de uma conversa do histórico (o id é o doc_id devolvido em [historico]). */
    @GET("chat/mensagens/{doc_id}")
    suspend fun mensagens(
        @Path("doc_id") docId: String,
    ): MensagensResponse
}
