package com.inventraoficial.inventra.data.remote.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import okhttp3.ResponseBody

/**
 * Um evento de POST /chat/stream (NDJSON: um JSON por linha). O app decide como desenhar cada tipo;
 * a IA não manda rótulos como "pensamento" ou "resposta" no texto, só o [type] do evento.
 */
sealed interface EventoIa {
    /**
     * Um passo do trabalho da IA (consultar o estoque, conferir a resposta...) começando ou terminando.
     * [pai] é o passo em que este roda (a tool dentro do nó); [detalhe] é o comando que ele executou.
     */
    data class Etapa(
        val id: String,
        val concluida: Boolean,
        val texto: String?,
        val pai: String? = null,
        val detalhe: String? = null,
    ) : EventoIa

    /** Pedaço do raciocínio do modelo, ao vivo. [id] separa especialistas que pensam ao mesmo tempo. */
    data class Pensamento(
        val id: String,
        val delta: String,
    ) : EventoIa

    /** Tabela ou gráfico prontos, inteiros (mesmo JSON de `artifacts` do POST /chat). */
    data class ArtefatoPronto(
        val dados: JsonElement,
    ) : EventoIa

    /** Ação proposta esperando o "sim" do usuário (mesmo formato de `pending_actions` do POST /chat). */
    data class AcaoProposta(
        val acao: AcaoPendenteDto,
    ) : EventoIa

    /** Pedaço da resposta final, que já passou pela conferência da IA. */
    data class Texto(
        val delta: String,
    ) : EventoIa

    /** Último evento. [respostaSegura] = a conferência trocou a resposta por uma frase segura. */
    data class Fim(
        val respostaSegura: Boolean,
    ) : EventoIa

    /** Falha depois de o stream abrir (o HTTP já tinha respondido 200). */
    data class Erro(
        val mensagem: String,
    ) : EventoIa

    /** Resposta inteira de uma IA sem streaming (volta para POST /chat quando /chat/stream não existe). */
    data class RespostaCompleta(
        val resposta: ChatResponse,
    ) : EventoIa
}

/** Lê uma linha do stream. Tipo desconhecido ou linha quebrada vira null: o app ignora e segue. */
fun interpretarEventoIa(
    linha: String,
    json: Json,
): EventoIa? =
    runCatching {
        val objeto = json.parseToJsonElement(linha) as? JsonObject ?: return@runCatching null

        fun texto(chave: String): String? = (objeto[chave] as? JsonPrimitive)?.contentOrNull

        when (texto("type")) {
            "etapa" -> {
                val id = texto("id") ?: return@runCatching null
                EventoIa.Etapa(
                    id = id,
                    concluida = texto("status") == "fim",
                    texto = texto("texto"),
                    pai = texto("pai"),
                    detalhe = texto("detalhe"),
                )
            }

            "pensamento" -> {
                val delta = texto("delta")?.takeIf { it.isNotEmpty() } ?: return@runCatching null
                EventoIa.Pensamento(id = texto("id").orEmpty(), delta = delta)
            }

            "artefato" -> objeto["artifact"]?.let { EventoIa.ArtefatoPronto(it) }

            "cartao" ->
                objeto["pending_action"]?.let {
                    EventoIa.AcaoProposta(json.decodeFromJsonElement(AcaoPendenteDto.serializer(), it))
                }

            "texto" -> texto("delta")?.takeIf { it.isNotEmpty() }?.let { EventoIa.Texto(it) }

            "fim" -> EventoIa.Fim(respostaSegura = texto("status") == "segura")

            "erro" -> EventoIa.Erro(texto("mensagem") ?: "A IA não conseguiu responder.")

            else -> null
        }
    }.getOrNull()

/**
 * Lê o corpo linha a linha, conforme chega. Linhas em branco são só o sinal de vida do servidor
 * (mantém a conexão aberta durante uma tool lenta) e são ignoradas.
 */
fun ResponseBody.eventosIa(json: Json): Flow<EventoIa> =
    flow {
        use { corpo ->
            val fonte = corpo.source()
            while (!fonte.exhausted()) {
                val linha = fonte.readUtf8Line() ?: break
                if (linha.isBlank()) continue
                interpretarEventoIa(linha, json)?.let { emit(it) }
            }
        }
    }.flowOn(Dispatchers.IO)
