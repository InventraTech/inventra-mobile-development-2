package com.inventraoficial.inventra.feature.assistant.ui

/**
 * Pedaço de texto com o estilo que o markdown da IA pediu. Mantido sem dependência do Compose
 * para o interpretador poder ser testado como Kotlin puro.
 */
internal data class TrechoMarkdown(
    val texto: String,
    val negrito: Boolean = false,
    val italico: Boolean = false,
    val codigo: Boolean = false,
)

private val REGRA_HORIZONTAL = Regex("""^\s*([-*_])(\s*\1){2,}\s*$""")
private val SEPARADOR_DE_TABELA = Regex("""^\|?\s*:?-{2,}:?\s*(\|\s*:?-{2,}:?\s*)*\|?$""")
private val TITULO = Regex("""^\s{0,3}#{1,6}\s+(.*)$""")
private val CITACAO = Regex("""^\s{0,3}>\s?(.*)$""")
private val MARCADOR = Regex("""^(\s*)[-*+•]\s+(.*)$""")
private val NUMERADO = Regex("""^(\s*)(\d+)[.)]\s+(.*)$""")
private val LINK = Regex("""^\[([^\]]+)]\(([^)\s]+)\)""")
private const val SEPARADOR_DE_CELULA = "  ·  "
private const val RECUO = "    "

// Espaço que não quebra linha: sem ele, um nome de arquivo comprido (sem espaços) vai para a linha de
// baixo e deixa a bolinha sozinha na linha de cima.
private const val ESPACO_FIXO = "\u00A0"

/**
 * Interpreta o subconjunto de markdown que a IA costuma devolver: títulos, negrito, itálico, código,
 * listas, citações e tabelas (estas viram linhas simples). Qualquer outra coisa sai como texto.
 */
internal fun interpretarMarkdown(markdown: String): List<TrechoMarkdown> {
    val linhas = markdown.replace("\r\n", "\n").trim('\n').split("\n")
    val linhasInterpretadas = mutableListOf<List<TrechoMarkdown>>()
    var dentroDeCodigo = false
    var linhaAnteriorEraTabela = false
    var linhaAnteriorEstavaEmBranco = false

    for (linha in linhas) {
        val aparada = linha.trim()
        val titulo = TITULO.matchEntire(linha)
        val citacao = CITACAO.matchEntire(linha)
        val marcador = MARCADOR.matchEntire(linha)
        val numerado = NUMERADO.matchEntire(linha)
        var ehTabela = false
        val trechos: List<TrechoMarkdown>? =
            when {
                aparada.startsWith("```") -> {
                    dentroDeCodigo = !dentroDeCodigo
                    null
                }
                dentroDeCodigo -> listOf(TrechoMarkdown(linha, codigo = true))
                aparada.isEmpty() -> emptyList()
                REGRA_HORIZONTAL.matches(aparada) -> null
                aparada.contains('|') && aparada.contains('-') && SEPARADOR_DE_TABELA.matches(aparada) -> {
                    ehTabela = true
                    null
                }
                aparada.startsWith("|") && aparada.count { it == '|' } >= 2 -> {
                    ehTabela = true
                    linhaDeTabela(aparada, cabecalho = !linhaAnteriorEraTabela)
                }
                titulo != null -> inline(titulo.groupValues[1], negrito = true)
                citacao != null -> inline(citacao.groupValues[1], italico = true)
                marcador != null ->
                    listOf(TrechoMarkdown(RECUO.repeat(marcador.groupValues[1].length / 2) + "•" + ESPACO_FIXO)) +
                        inline(marcador.groupValues[2])
                numerado != null ->
                    listOf(
                        TrechoMarkdown(
                            RECUO.repeat(numerado.groupValues[1].length / 2) +
                                numerado.groupValues[2] + "." + ESPACO_FIXO,
                        ),
                    ) + inline(numerado.groupValues[3])
                else -> inline(aparada)
            }

        if (ehTabela) {
            linhaAnteriorEraTabela = true
        } else if (trechos != null) {
            linhaAnteriorEraTabela = false
        }

        if (trechos == null) continue
        // Várias linhas em branco seguidas viram uma só.
        if (trechos.isEmpty() && linhaAnteriorEstavaEmBranco) continue
        linhaAnteriorEstavaEmBranco = trechos.isEmpty()
        linhasInterpretadas.add(trechos)
    }

    val saida = mutableListOf<TrechoMarkdown>()
    linhasInterpretadas.forEachIndexed { indice, trechos ->
        if (indice > 0) saida += TrechoMarkdown("\n")
        saida += trechos
    }
    return unirTrechosIguais(saida)
}

private fun linhaDeTabela(
    linha: String,
    cabecalho: Boolean,
): List<TrechoMarkdown> {
    val celulas = linha.trim('|').split('|').map { it.trim() }
    val saida = mutableListOf<TrechoMarkdown>()
    celulas.forEachIndexed { indice, celula ->
        if (indice > 0) saida += TrechoMarkdown(SEPARADOR_DE_CELULA)
        saida += inline(celula, negrito = cabecalho)
    }
    return saida
}

private fun inline(
    texto: String,
    negrito: Boolean = false,
    italico: Boolean = false,
): List<TrechoMarkdown> {
    val saida = mutableListOf<TrechoMarkdown>()
    val pendente = StringBuilder()

    fun descarregar() {
        if (pendente.isNotEmpty()) {
            saida += TrechoMarkdown(pendente.toString(), negrito, italico)
            pendente.clear()
        }
    }

    var i = 0
    while (i < texto.length) {
        val c = texto[i]
        val limiteEsquerdo = i == 0 || !texto[i - 1].isLetterOrDigit()
        when {
            c == '\\' && i + 1 < texto.length && texto[i + 1] in "\\`*_{}[]()#+-.!|>" -> {
                pendente.append(texto[i + 1])
                i += 2
            }
            texto.startsWith("***", i) && texto.indexOf("***", i + 3) > i + 3 -> {
                val fim = texto.indexOf("***", i + 3)
                descarregar()
                saida += inline(texto.substring(i + 3, fim), negrito = true, italico = true)
                i = fim + 3
            }
            (texto.startsWith("**", i) || (texto.startsWith("__", i) && limiteEsquerdo)) &&
                texto.indexOf(texto.substring(i, i + 2), i + 2) > i + 2 -> {
                val marca = texto.substring(i, i + 2)
                val fim = texto.indexOf(marca, i + 2)
                descarregar()
                saida += inline(texto.substring(i + 2, fim), negrito = true, italico = italico)
                i = fim + 2
            }
            c == '`' && texto.indexOf('`', i + 1) > i -> {
                val fim = texto.indexOf('`', i + 1)
                descarregar()
                saida += TrechoMarkdown(texto.substring(i + 1, fim), negrito, italico, codigo = true)
                i = fim + 1
            }
            (c == '*' || (c == '_' && limiteEsquerdo)) && fechamentoDeItalico(texto, i) > 0 -> {
                val fim = fechamentoDeItalico(texto, i)
                descarregar()
                saida += inline(texto.substring(i + 1, fim), negrito = negrito, italico = true)
                i = fim + 1
            }
            c == '[' && LINK.find(texto.substring(i)) != null -> {
                val achado = LINK.find(texto.substring(i))!!
                pendente.append(achado.groupValues[1])
                i += achado.value.length
            }
            else -> {
                pendente.append(c)
                i++
            }
        }
    }
    descarregar()
    return saida
}

/** Posição do marcador que fecha o itálico aberto em [inicio], ou -1 se não houver um fechamento válido. */
private fun fechamentoDeItalico(
    texto: String,
    inicio: Int,
): Int {
    val marca = texto[inicio]
    if (inicio + 1 >= texto.length || texto[inicio + 1].isWhitespace() || texto[inicio + 1] == marca) return -1
    var j = inicio + 1
    while (j < texto.length) {
        if (texto[j] == marca && !texto[j - 1].isWhitespace() && texto.getOrNull(j + 1) != marca) {
            val limiteDireito = marca == '*' || j + 1 >= texto.length || !texto[j + 1].isLetterOrDigit()
            if (limiteDireito) return j
        }
        j++
    }
    return -1
}

private fun unirTrechosIguais(trechos: List<TrechoMarkdown>): List<TrechoMarkdown> {
    val saida = mutableListOf<TrechoMarkdown>()
    for (trecho in trechos) {
        val ultimo = saida.lastOrNull()
        if (ultimo != null &&
            ultimo.negrito == trecho.negrito &&
            ultimo.italico == trecho.italico &&
            ultimo.codigo == trecho.codigo
        ) {
            saida[saida.size - 1] = ultimo.copy(texto = ultimo.texto + trecho.texto)
        } else {
            saida += trecho
        }
    }
    return saida
}
