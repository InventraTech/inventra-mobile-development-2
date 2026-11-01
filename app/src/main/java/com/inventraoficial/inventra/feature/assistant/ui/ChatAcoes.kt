package com.inventraoficial.inventra.feature.assistant.ui

import com.inventraoficial.inventra.data.remote.api.AcaoPendenteDto
import com.inventraoficial.inventra.data.remote.api.AprovacaoDto
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Ações que a IA propôs e esperam o "sim" do usuário. Nada aqui depende de Android: dá para testar direto.

enum class EstadoDaAcao {
    /** Esperando o usuário aprovar ou recusar. */
    AGUARDANDO,
    APROVANDO,
    RECUSANDO,

    /** Aprovada, mas a execução ainda não terminou (ou o app não soube do fim). */
    APROVADA,

    /** O app está pedindo para o servidor executar. */
    EXECUTANDO,
    CONCLUIDA,
    RECUSADA,
    FALHOU,
    EXPIRADA,

    /** O servidor não sabe se a execução gravou; não pode repetir sem conferir. */
    INCERTA,
}

data class LinhaDaAcao(
    val rotulo: String,
    val valor: String,
)

/** Saldo do lote antes e depois da ação, já com a unidade ("85 kg" → "75 kg"). */
data class MudancaDeSaldo(
    val antes: String,
    val depois: String,
)

data class AcaoPendente(
    val id: String,
    val propostaHash: String,
    val titulo: String,
    val linhas: List<LinhaDaAcao>,
    val justificativa: String,
    val expiraEmMillis: Long?,
    val estado: EstadoDaAcao = EstadoDaAcao.AGUARDANDO,
    /** Resultado, motivo da falha ou aviso de erro de rede para mostrar no cartão. */
    val detalhe: String? = null,
    /** O resultado veio de dados de demonstração (a IA em modo mock). */
    val simulado: Boolean = false,
    /** Como o saldo do lote fica depois da ação, quando ela mexe no saldo de um lote. */
    val mudanca: MudancaDeSaldo? = null,
)

private val ROTULOS_OPERACAO =
    mapOf(
        "criar_produto" to "Cadastrar produto",
        "editar_produto" to "Editar produto",
        "desativar_produto" to "Desativar produto",
        "cadastrar_fornecedor" to "Cadastrar fornecedor",
        "desativar_fornecedor" to "Desativar fornecedor",
        "criar_lote" to "Criar lote de estoque",
        "registrar_entrada" to "Registrar entrada em lote",
        "registrar_saida" to "Registrar saída de estoque",
        "ajustar_estoque" to "Ajustar estoque",
        "criar_requisicao" to "Criar requisição",
        "adicionar_item_requisicao" to "Adicionar item à requisição",
        "remover_item_requisicao" to "Remover item da requisição",
        "cancelar_requisicao" to "Cancelar requisição",
        "registrar_inventario" to "Abrir inventário",
        "registrar_contagem" to "Registrar contagem",
        "alterar_contagem" to "Alterar contagem",
    )

private val ROTULOS_PARAMETRO =
    mapOf(
        "type" to "Tipo",
        "origin" to "Origem",
        "productId" to "Produto",
        "supplierId" to "Fornecedor",
        "suggestedSupplierId" to "Fornecedor sugerido",
        "batchId" to "Lote",
        "requisitionId" to "Requisição",
        "inventoryId" to "Inventário",
        "itemId" to "Item",
        "countId" to "Contagem",
        "quantity" to "Quantidade",
        "newQuantity" to "Nova quantidade",
        "initialQuantity" to "Quantidade inicial",
        "physicalQuantity" to "Quantidade física",
        "estimatedPrice" to "Preço estimado",
        "unitPrice" to "Preço unitário",
        "batchNumber" to "Número do lote",
        "invoiceNumber" to "Nota fiscal",
        "entryDate" to "Entrada",
        "expirationDate" to "Validade",
        "note" to "Observação",
        "reason" to "Motivo",
        "name" to "Nome",
        "brand" to "Marca",
    )

private val VALORES_CONHECIDOS =
    mapOf(
        "PURCHASE" to "Compra",
        "TRANSFER" to "Transferência",
        "CONSUMPTION" to "Consumo",
    )

private val PARAMETROS_COM_UNIDADE = setOf("quantity", "newQuantity", "initialQuantity", "physicalQuantity")

private val PARAMETROS_DE_PRECO = setOf("estimatedPrice", "unitPrice")

internal const val TEXTO_ACAO_EXPIRADA = "O prazo para confirmar acabou. Peça de novo se ainda precisar."

private val FORMATO_DO_PRAZO = DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm")
private const val PARAMETRO_DO_ALVO = "parameter"
private const val REGISTRO_DO_ALVO = "record"
private const val SUFIXO_DE_ID = "Id"
private const val STATUS_AGUARDANDO = "AGUARDANDO_APROVACAO"
private const val TEXTO_ESTADO_DA_PROPOSTA = "- Estado:"
private val DECIMAL_COM_PONTO = Regex("^-?\\d+\\.\\d+$")
private val PARAGRAFO_SEM_ALTERACAO = Regex("\\n*Nada foi alterado nesta conversa\\.[^\\n]*")
private val CABECALHO_DA_PROPOSTA = Regex("^\\*\\*.+\\*\\* — proposta `[^`]*`$")
private val LINHAS_EM_BRANCO_SEGUIDAS = Regex("\\n{3,}")
private const val TEXTO_RESUMO_DAS_PROPOSTAS = "Somente as propostas listadas"

/** Monta o cartão de confirmação a partir do que a IA mandou em `pending_actions`. */
internal fun AcaoPendenteDto.paraAcao(): AcaoPendente =
    AcaoPendente(
        id = id,
        propostaHash = proposal_hash,
        titulo = ROTULOS_OPERACAO[operation] ?: operation.replace('_', ' ').replaceFirstChar { it.uppercase() },
        linhas = linhasDaAcao(parameters, before),
        justificativa = justification,
        expiraEmMillis = interpretarDataIso(expires_at)?.toEpochMilli(),
        estado = estadoDoServidor(status) ?: EstadoDaAcao.AGUARDANDO,
        mudanca = mudancaDeSaldo(operation, parameters, before),
    )

/** Aplica no cartão o que o servidor respondeu depois de aprovar, recusar, executar ou consultar. */
internal fun AprovacaoDto.aplicarEm(acao: AcaoPendente): AcaoPendente {
    val estado = estadoDoServidor(status) ?: EstadoDaAcao.FALHOU
    val mensagem = result?.message?.takeIf { it.isNotBlank() }
    val detalhe =
        when (estado) {
            // O cartão já diz "Concluída"; frase do servidor ("Resultado confirmado pela API") é ruído.
            EstadoDaAcao.CONCLUIDA -> null
            EstadoDaAcao.FALHOU ->
                mensagem ?: execution_error?.takeIf { it.isNotBlank() } ?: "Não consegui concluir essa ação."
            EstadoDaAcao.INCERTA ->
                "Não tenho certeza se foi gravado. Confira no sistema antes de repetir; não vou tentar de novo sozinho."
            EstadoDaAcao.APROVADA -> "Aprovada. A execução ainda não terminou."
            EstadoDaAcao.EXPIRADA -> TEXTO_ACAO_EXPIRADA
            else -> null
        }
    return acao.copy(estado = estado, detalhe = detalhe, simulado = result?.mocked ?: acao.simulado)
}

/** Estados do servidor traduzidos para os do cartão; null quando o texto é desconhecido. */
internal fun estadoDoServidor(status: String): EstadoDaAcao? =
    when (status) {
        STATUS_AGUARDANDO -> EstadoDaAcao.AGUARDANDO
        "APROVADA_PENDENTE_INTEGRACAO", "EM_EXECUCAO" -> EstadoDaAcao.APROVADA
        "CONCLUIDA" -> EstadoDaAcao.CONCLUIDA
        "RECUSADA" -> EstadoDaAcao.RECUSADA
        "FALHOU" -> EstadoDaAcao.FALHOU
        "EXPIRADA" -> EstadoDaAcao.EXPIRADA
        "RESULTADO_INCERTO" -> EstadoDaAcao.INCERTA
        else -> null
    }

/** "03/11 às 08:08", na hora do aparelho. */
internal fun prazoLegivel(
    millis: Long,
    zona: ZoneId = ZoneId.systemDefault(),
): String = FORMATO_DO_PRAZO.withZone(zona).format(Instant.ofEpochMilli(millis))

/** O prazo passou e o cartão ainda esperava resposta: os botões não servem mais. */
internal fun AcaoPendente.expirou(agoraMillis: Long): Boolean =
    estado == EstadoDaAcao.AGUARDANDO && expiraEmMillis != null && agoraMillis > expiraEmMillis

/** O usuário ainda pode aprovar ou recusar? */
internal fun AcaoPendente.podeDecidir(agoraMillis: Long) = estado == EstadoDaAcao.AGUARDANDO && !expirou(agoraMillis)

/**
 * O texto da IA repete cada proposta (título, parâmetros, "Estado: aguardando") e diz que nada foi alterado.
 * Isso duplica o cartão e fica errado assim que o usuário decide, então o cartão (que mostra os dados e
 * acompanha o estado real) assume tudo isso. Sobra só o que o cartão não diz, como etapas que falharam.
 */
internal fun limparTextoDasPropostas(texto: String): String {
    var dentroDeUmaProposta = false
    val restantes = mutableListOf<String>()
    for (linha in texto.lines()) {
        val limpa = linha.trim()
        when {
            CABECALHO_DA_PROPOSTA.matches(limpa) -> dentroDeUmaProposta = true
            dentroDeUmaProposta && limpa.startsWith("- ") -> Unit
            limpa.startsWith(TEXTO_ESTADO_DA_PROPOSTA) -> Unit
            limpa.startsWith(TEXTO_RESUMO_DAS_PROPOSTAS) -> dentroDeUmaProposta = false
            else -> {
                dentroDeUmaProposta = false
                restantes += linha
            }
        }
    }
    return restantes
        .joinToString("\n")
        .replace(PARAGRAFO_SEM_ALTERACAO, "")
        .replace(LINHAS_EM_BRANCO_SEGUIDAS, "\n\n")
        .trim()
}

/** Frase curta que acompanha os cartões: eles trazem os detalhes, o texto só diz o que fazer. */
internal fun resumoDasPropostas(quantidade: Int): String {
    val proposta = if (quantidade == 1) "uma proposta" else "$quantidade propostas"
    return "Preparei $proposta para você revisar. Nada é alterado até você tocar em **Aprovar**."
}

/**
 * Saldo do lote antes e depois: saída tira a quantidade, entrada soma, ajuste define a nova quantidade.
 * Null quando a ação não mexe no saldo de um lote ou o snapshot não traz o saldo atual.
 */
@Suppress("ReturnCount")
internal fun mudancaDeSaldo(
    operacao: String,
    parametros: JsonObject,
    alvos: List<JsonObject>,
): MudancaDeSaldo? {
    val lote = registroDoAlvo(alvos, "batchId") ?: return null
    val atual = lote["currentQuantity"].textoOuNull()?.toBigDecimalOrNull() ?: return null
    val quantidade = parametros["quantity"].textoOuNull()?.toBigDecimalOrNull()
    val depois =
        when (operacao) {
            "registrar_saida" -> quantidade?.let { atual - it }
            "registrar_entrada" -> quantidade?.let { atual + it }
            "ajustar_estoque" -> parametros["newQuantity"].textoOuNull()?.toBigDecimalOrNull()
            else -> null
        } ?: return null
    val unidade = unidadeDosAlvos(alvos)
    return MudancaDeSaldo(saldoLegivel(atual, unidade), saldoLegivel(depois, unidade))
}

private fun saldoLegivel(
    valor: java.math.BigDecimal,
    unidade: String?,
): String = decimalBrasileiro(valor.stripTrailingZeros().toPlainString()) + (unidade?.let { " $it" } ?: "")

private fun linhasDaAcao(
    parametros: JsonObject,
    alvos: List<JsonObject>,
): List<LinhaDaAcao> {
    val unidade = unidadeDosAlvos(alvos)
    return parametros.entries.map { (chave, valor) ->
        LinhaDaAcao(
            rotulo = ROTULOS_PARAMETRO[chave] ?: chave,
            valor = valorDoParametro(chave, valor, alvos, unidade),
        )
    }
}

private fun valorDoParametro(
    chave: String,
    valor: JsonElement,
    alvos: List<JsonObject>,
    unidade: String?,
): String {
    val texto = textoDoValor(valor)
    return when {
        chave.endsWith(SUFIXO_DE_ID) -> {
            // O número do registro sai como veio (1234, não "1.234").
            val numero = (valor as? JsonPrimitive)?.contentOrNull ?: texto
            val nome = nomeDoAlvo(alvos, chave)
            if (nome != null) "$nome (nº $numero)" else "nº $numero"
        }
        chave in PARAMETROS_COM_UNIDADE -> decimalBrasileiro(texto) + (unidade?.let { " $it" } ?: "")
        chave in PARAMETROS_DE_PRECO -> decimalBrasileiro(texto)
        else -> VALORES_CONHECIDOS[texto] ?: texto
    }
}

private fun textoDoValor(valor: JsonElement): String = formatarCelula(valor)

/** Decimais da IA vêm com ponto ("2.5"); o usuário lê com vírgula. */
private fun decimalBrasileiro(texto: String) = if (DECIMAL_COM_PONTO.matches(texto)) texto.replace('.', ',') else texto

private fun JsonElement?.textoOuNull(): String? = (this as? JsonPrimitive)?.contentOrNull

private fun registroDoAlvo(
    alvos: List<JsonObject>,
    parametro: String,
): JsonObject? =
    alvos
        .firstOrNull { it[PARAMETRO_DO_ALVO].textoOuNull() == parametro }
        ?.get(REGISTRO_DO_ALVO)
        ?.let { it as? JsonObject }

/** Nome legível do registro guardado no snapshot da proposta (produto, fornecedor, lote...). */
private fun nomeDoAlvo(
    alvos: List<JsonObject>,
    parametro: String,
): String? {
    val registro = registroDoAlvo(alvos, parametro) ?: return null
    val produto = registro["product"] as? JsonObject
    val nome =
        registro["name"].textoOuNull()
            ?: produto?.get("name").textoOuNull()
            ?: registro["legalName"].textoOuNull()
    val lote = registro["batchNumber"].textoOuNull()
    return when {
        nome != null && lote != null && parametro == "batchId" -> "$nome, lote $lote"
        else -> nome ?: lote
    }
}

/** Unidade de medida (kg, L...) do produto ou lote envolvido, quando o snapshot a traz. */
private fun unidadeDosAlvos(alvos: List<JsonObject>): String? =
    alvos.firstNotNullOfOrNull { alvo ->
        val registro = alvo[REGISTRO_DO_ALVO] as? JsonObject ?: return@firstNotNullOfOrNull null
        val produto = registro["product"] as? JsonObject
        val unidade = (produto?.get("unit") ?: registro["unit"]) as? JsonObject
        unidade?.get("symbol").textoOuNull()?.takeIf { it.isNotBlank() }
    }

/** A ação com esse id, esteja na conversa que aparece ou na conversa ao vivo guardada atrás do histórico. */
internal fun ChatUiState.acaoPorId(acaoId: String): AcaoPendente? =
    (mensagens + conversaEmAndamento?.mensagens.orEmpty()).flatMap { it.acoes }.firstOrNull { it.id == acaoId }

/** Troca só o cartão com esse id (nas duas conversas, se for o caso) e deixa o resto como está. */
internal fun ChatUiState.comAcaoAtualizada(
    acaoId: String,
    bloco: (AcaoPendente) -> AcaoPendente,
): ChatUiState {
    fun List<Mensagem>.atualizada(): List<Mensagem> =
        map { mensagem ->
            if (mensagem.acoes.none { it.id == acaoId }) {
                mensagem
            } else {
                mensagem.copy(acoes = mensagem.acoes.map { if (it.id == acaoId) bloco(it) else it })
            }
        }
    return copy(
        mensagens = mensagens.atualizada(),
        conversaEmAndamento = conversaEmAndamento?.let { it.copy(mensagens = it.mensagens.atualizada()) },
    )
}
