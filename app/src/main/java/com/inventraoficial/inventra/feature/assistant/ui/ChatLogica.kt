package com.inventraoficial.inventra.feature.assistant.ui

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID

// Regras do chat que não dependem de Android: ficam aqui para serem testadas direto.

private const val TAMANHO_MAXIMO_DO_TITULO = 48
private const val DIAS_NA_SEMANA = 7L
private const val DIAS_NO_MES = 30L
private const val PASSO_MINIMO_DA_DIGITACAO = 3
private const val DIVISOR_DO_PASSO_DA_DIGITACAO = 100

internal fun novaSessaoId(): String = UUID.randomUUID().toString()

internal fun novoId(prefixo: String): String = "${prefixo}_${UUID.randomUUID()}"

// ---------- fluxo da resposta ----------

/** Um passo de primeiro nível com os passos que rodaram dentro dele (as tools do nó). */
internal data class PassoComFilhos(
    val passo: PassoIa,
    val filhos: List<PassoIa>,
)

/**
 * Agrupa os passos: cada tool aparece sob o nó em que rodou, na ordem em que chegou. Passo cujo pai
 * não existe (ou sem pai) fica no primeiro nível, então nada some.
 */
internal fun agruparPassos(passos: List<PassoIa>): List<PassoComFilhos> {
    val ids = passos.map { it.id }.toSet()
    val raizes = passos.filter { it.pai == null || it.pai !in ids }
    return raizes.map { raiz -> PassoComFilhos(raiz, passos.filter { it.pai == raiz.id }) }
}

private const val TAMANHO_DA_PREVIA = 70
private val MARCAS_DA_PREVIA = Regex("[*_`#>]")

/** Primeira linha do raciocínio, sem marcas de markdown: o que aparece quando ele está recolhido. */
internal fun previaDoPensamento(pensamento: String): String {
    val linha =
        pensamento
            .lineSequence()
            .map { it.replace(MARCAS_DA_PREVIA, "").trim() }
            .firstOrNull { it.isNotEmpty() }
            .orEmpty()
    return if (linha.length <= TAMANHO_DA_PREVIA) linha else linha.take(TAMANHO_DA_PREVIA - 1).trimEnd() + "…"
}

// ---------- tempo da resposta ----------

private const val SEGUNDOS_POR_MINUTO = 60L

/** "8 s" ou "1 min 05 s": quanto a IA está levando (ou levou) para responder. */
internal fun formatarDuracao(segundos: Long): String {
    val total = segundos.coerceAtLeast(0)
    return if (total < SEGUNDOS_POR_MINUTO) {
        "$total s"
    } else {
        "${total / SEGUNDOS_POR_MINUTO} min ${(total % SEGUNDOS_POR_MINUTO).toString().padStart(2, '0')} s"
    }
}

// ---------- saudação ----------

/** "Olá, João!" com o primeiro nome; sem nome, só "Olá!". */
internal fun saudacaoPara(nome: String?): String {
    val primeiro = nome?.trim()?.substringBefore(' ')?.takeIf { it.isNotEmpty() } ?: return "Olá!"
    return "Olá, $primeiro!"
}

/** Conversa vazia com um novo id de sessão, mantendo só o que é da tela (saudação e histórico). */
internal fun ChatUiState.novaConversaLimpa(): ChatUiState =
    ChatUiState(
        greeting = greeting,
        conversas = conversas,
        sessionId = novaSessaoId(),
    )

/** A conversa "de verdade" do usuário: a guardada, quando ele está olhando o histórico. */
internal fun ChatUiState.conversaAtiva(): ChatUiState =
    if (conversaVisualizadaId != null) conversaEmAndamento ?: novaConversaLimpa() else this

// ---------- histórico (menu lateral) ----------

internal data class GrupoDeConversas(
    val titulo: String,
    val conversas: List<ConversaResumo>,
)

/** Lê a data da IA, com ou sem fuso ("...Z", "...+00:00" ou sem sufixo, que é UTC). */
internal fun interpretarDataIso(texto: String): Instant? {
    val limpo = texto.trim()
    return runCatching { OffsetDateTime.parse(limpo).toInstant() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(limpo).toInstant(ZoneOffset.UTC) }.getOrNull()
}

private val PREFIXO_DO_RESUMO = Regex("^(o|a)\\s+(usuári[oa]|funcionári[oa])\\s+", RegexOption.IGNORE_CASE)
private val MARCAS_DE_MARKDOWN = Regex("[*_`#>]")
private val ESPACOS = Regex("\\s+")

/** Título curto para o menu, tirado da primeira frase do resumo que a IA gerou. */
internal fun tituloDaConversa(resumo: String): String {
    val limpo = resumo.replace(MARCAS_DE_MARKDOWN, "").replace(ESPACOS, " ").trim()
    if (limpo.isEmpty()) return "Conversa sem título"
    val primeiraFrase = limpo.substringBefore(". ").trimEnd('.').replace(PREFIXO_DO_RESUMO, "")
    val capitalizada = primeiraFrase.replaceFirstChar { it.uppercase() }
    if (capitalizada.isEmpty()) return "Conversa sem título"
    return if (capitalizada.length <= TAMANHO_MAXIMO_DO_TITULO) {
        capitalizada
    } else {
        capitalizada.take(TAMANHO_MAXIMO_DO_TITULO - 1).trimEnd() + "…"
    }
}

/** Agrupa como os assistentes de IA: Hoje, Ontem, Últimos 7 dias, Últimos 30 dias e Mais antigas. */
internal fun agruparPorData(
    conversas: List<ConversaResumo>,
    hoje: LocalDate,
    zona: ZoneId,
): List<GrupoDeConversas> {
    val porTitulo = linkedMapOf<String, MutableList<ConversaResumo>>()
    for (conversa in conversas.sortedByDescending { it.iniciadaEmMillis }) {
        val dia = Instant.ofEpochMilli(conversa.iniciadaEmMillis).atZone(zona).toLocalDate()
        val dias = ChronoUnit.DAYS.between(dia, hoje)
        val titulo =
            when {
                dias <= 0 -> "Hoje"
                dias == 1L -> "Ontem"
                dias <= DIAS_NA_SEMANA -> "Últimos 7 dias"
                dias <= DIAS_NO_MES -> "Últimos 30 dias"
                else -> "Mais antigas"
            }
        porTitulo.getOrPut(titulo) { mutableListOf() }.add(conversa)
    }
    return porTitulo.map { (titulo, itens) -> GrupoDeConversas(titulo, itens) }
}

// ---------- resposta "digitada" ----------

private fun passoDaDigitacao(total: Int): Int = maxOf(PASSO_MINIMO_DA_DIGITACAO, total / DIVISOR_DO_PASSO_DA_DIGITACAO)

/**
 * Quantos caracteres do [texto] devem aparecer no próximo "toque" da digitação.
 * Avança em pedaços proporcionais ao tamanho (a resposta inteira leva ~3 s) e termina a palavra.
 */
internal fun proximoLimiteDaDigitacao(
    texto: String,
    visiveis: Int,
): Int {
    if (visiveis >= texto.length) return texto.length
    var fim = minOf(texto.length, visiveis + passoDaDigitacao(texto.length))
    while (fim < texto.length && !texto[fim].isWhitespace()) fim++
    return fim
}

/** Evita mostrar "**" ou "`" soltos enquanto o texto ainda está pela metade. */
internal fun fecharMarcadoresAbertos(parcial: String): String {
    var resultado = parcial
    if (Regex("\\*\\*").findAll(parcial).count() % 2 == 1) resultado += "**"
    if (parcial.count { it == '`' } % 2 == 1) resultado += "`"
    return resultado
}
