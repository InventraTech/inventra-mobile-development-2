package com.inventraoficial.inventra.feature.assistant.ui

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.text.NumberFormat
import java.util.Locale

/** Tabela ou gráfico que a IA montou com dados confirmados do Inventra (no máximo um por resposta). */
sealed interface Artefato {
    /** Dados de demonstração (a IA estava em modo mock): a tela avisa para ninguém decidir em cima deles. */
    val simulado: Boolean

    /** Aviso da IA sobre como ler os dados (ex.: "estimativa pela baixa dos lotes", itens que ficaram de fora). */
    val nota: String?

    data class Tabela(
        val colunas: List<ColunaDaTabela>,
        val linhas: List<List<String>>,
        override val simulado: Boolean,
        override val nota: String? = null,
    ) : Artefato

    data class Grafico(
        val tipo: TipoDeGrafico,
        val categorias: List<String>,
        val series: List<SerieDoGrafico>,
        val unidade: String?,
        override val simulado: Boolean,
        override val nota: String? = null,
    ) : Artefato
}

data class ColunaDaTabela(
    val chave: String,
    val titulo: String,
)

data class SerieDoGrafico(
    val nome: String,
    val valores: List<Double>,
)

enum class TipoDeGrafico { BARRAS, PIZZA }

/** Uma fatia (ou barra) já pronta para desenhar. */
data class Fatia(
    val nome: String,
    val valor: Double,
)

private val DATA_ISO = Regex("^\\d{4}-\\d{2}-\\d{2}$")
private val PARECE_NUMERO = Regex("^-?[\\d.]+(,\\d+)?$")
private const val CELULA_VAZIA = "—"
private const val CASAS_DECIMAIS = 2

/**
 * Converte o que a IA mandou em [Artefato]s. Lê com tolerância: um artefato que não entendemos
 * (tipo novo, campo faltando) é ignorado, e a resposta em texto continua aparecendo normalmente.
 */
internal fun lerArtefatos(elementos: List<JsonElement>): List<Artefato> = elementos.mapNotNull { lerArtefato(it) }

internal fun lerArtefato(elemento: JsonElement): Artefato? =
    runCatching {
        val objeto = elemento.jsonObject
        val simulado = objeto["mocked"]?.jsonPrimitive?.booleanOrNull ?: false
        val nota = objeto["note"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
        when (objeto["type"]?.jsonPrimitive?.contentOrNull) {
            "table" -> lerTabela(objeto, simulado, nota)
            "chart" -> lerGrafico(objeto, simulado, nota)
            else -> null
        }
    }.getOrNull()

private fun lerTabela(
    objeto: Map<String, JsonElement>,
    simulado: Boolean,
    nota: String?,
): Artefato.Tabela? {
    val colunas =
        objeto.getValue("columns").jsonArray.map {
            val coluna = it.jsonObject
            ColunaDaTabela(
                chave = coluna.getValue("key").jsonPrimitive.content,
                titulo = coluna.getValue("label").jsonPrimitive.content,
            )
        }
    val linhas =
        objeto.getValue("rows").jsonArray.map { linha ->
            val celulas = linha.jsonObject
            colunas.map { coluna -> formatarCelula(celulas[coluna.chave]) }
        }
    return if (colunas.isEmpty() || linhas.isEmpty()) null else Artefato.Tabela(colunas, linhas, simulado, nota)
}

private fun lerGrafico(
    objeto: Map<String, JsonElement>,
    simulado: Boolean,
    nota: String?,
): Artefato.Grafico? {
    val tipo =
        when (objeto.getValue("chart_type").jsonPrimitive.content) {
            "bar" -> TipoDeGrafico.BARRAS
            "pie" -> TipoDeGrafico.PIZZA
            else -> return null
        }
    val categorias = objeto.getValue("labels").jsonArray.map { it.jsonPrimitive.content }
    val series =
        objeto.getValue("datasets").jsonArray.map {
            val conjunto = it.jsonObject
            SerieDoGrafico(
                nome = conjunto.getValue("label").jsonPrimitive.content,
                valores = conjunto.getValue("data").jsonArray.map { v -> v.jsonPrimitive.doubleOrNull ?: 0.0 },
            )
        }
    val coerente = series.isNotEmpty() && series.all { it.valores.size == categorias.size }
    if (categorias.isEmpty() || !coerente) return null
    val unidade = objeto["unit"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
    return Artefato.Grafico(tipo, categorias, series, unidade, simulado, nota)
}

/** Texto de uma célula: vazio vira "—", datas viram dd/MM/aaaa e números usam vírgula decimal. */
internal fun formatarCelula(valor: JsonElement?): String {
    if (valor == null || valor is JsonNull) return CELULA_VAZIA
    val primitivo = valor as? JsonPrimitive ?: return valor.toString()
    if (primitivo.isString) {
        val texto = primitivo.content
        return when {
            texto.isBlank() -> CELULA_VAZIA
            DATA_ISO.matches(texto) -> dataBrasileira(texto)
            else -> texto
        }
    }
    val numero = primitivo.doubleOrNull ?: return primitivo.content
    return formatarNumero(numero)
}

/** "2026-11-05" -> "05/11/2026". */
internal fun dataBrasileira(dataIso: String): String {
    val (ano, mes, dia) = dataIso.split("-")
    return "$dia/$mes/$ano"
}

/** 1234.5 -> "1.234,5"; inteiros saem sem casas decimais. */
internal fun formatarNumero(valor: Double): String {
    val formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"))
    formato.maximumFractionDigits = if (valor == Math.rint(valor)) 0 else CASAS_DECIMAIS
    return formato.format(valor)
}

/** Parte do total em %, sem casas decimais; abaixo de 1% vira "<1%". */
internal fun percentualLegivel(
    valor: Double,
    total: Double,
): String {
    if (total <= 0.0) return "0%"
    val percentual = valor / total * PERCENTUAL_CHEIO
    return if (percentual > 0.0 && percentual < 1.0) "<1%" else "${Math.round(percentual)}%"
}

/** Número seguido da unidade, quando o gráfico tem uma ("12 kg"). */
internal fun formatarValor(
    valor: Double,
    unidade: String?,
): String = formatarNumero(valor) + (unidade?.let { " $it" } ?: "")

/** A coluna só tem números? Então a tela alinha à direita. */
internal fun colunaEhNumerica(
    tabela: Artefato.Tabela,
    indice: Int,
): Boolean =
    tabela.linhas.all {
        val celula = it[indice]
        celula == CELULA_VAZIA || PARECE_NUMERO.matches(celula)
    }

/**
 * Pizza com muitas categorias fica ilegível: mantém as [maximo] - 1 maiores e junta o resto em "Outros".
 * Barras mantêm a ordem em que a IA mandou.
 */
internal fun agruparFatias(
    categorias: List<String>,
    valores: List<Double>,
    maximo: Int,
): List<Fatia> {
    val todas = categorias.zip(valores) { nome, valor -> Fatia(nome, valor) }
    if (todas.size <= maximo) return todas
    val ordenadas = todas.sortedByDescending { it.valor }
    val principais = ordenadas.take(maximo - 1)
    val resto = ordenadas.drop(maximo - 1).sumOf { it.valor }
    return principais + Fatia("Outros", resto)
}

/** Descrição falada do gráfico/tabela para leitores de tela (a tela é só desenho). */
internal fun descricaoDoArtefato(artefato: Artefato): String =
    when (artefato) {
        is Artefato.Tabela -> {
            val titulos = artefato.colunas.joinToString(", ") { it.titulo }
            "Tabela com ${artefato.linhas.size} linhas e as colunas $titulos."
        }
        is Artefato.Grafico -> {
            val tipo = if (artefato.tipo == TipoDeGrafico.PIZZA) "Gráfico de pizza" else "Gráfico de barras"
            val itens =
                artefato.categorias
                    .zip(artefato.series.first().valores)
                    .take(ITENS_NA_DESCRICAO)
                    .joinToString("; ") { (nome, valor) -> "$nome ${formatarValor(valor, artefato.unidade)}" }
            "$tipo com ${artefato.categorias.size} itens: $itens."
        }
    }

private const val ITENS_NA_DESCRICAO = 10
private const val PERCENTUAL_CHEIO = 100.0
