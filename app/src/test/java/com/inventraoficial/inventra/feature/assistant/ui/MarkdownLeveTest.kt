package com.inventraoficial.inventra.feature.assistant.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class MarkdownLeveTest {
    private fun texto(markdown: String) = interpretarMarkdown(markdown).joinToString("") { it.texto }

    @Test
    fun `texto simples passa sem alteracao`() {
        assertEquals(listOf(TrechoMarkdown("Olá, tudo bem?")), interpretarMarkdown("Olá, tudo bem?"))
    }

    @Test
    fun `negrito e italico viram estilos e somem os marcadores`() {
        assertEquals(
            listOf(
                TrechoMarkdown("Arroz", negrito = true),
                TrechoMarkdown(": 25 kg, "),
                TrechoMarkdown("vence logo", italico = true),
            ),
            interpretarMarkdown("**Arroz**: 25 kg, *vence logo*"),
        )
    }

    @Test
    fun `negrito com sublinhado duplo e italico com sublinhado`() {
        assertEquals(
            listOf(
                TrechoMarkdown("forte", negrito = true),
                TrechoMarkdown(" e "),
                TrechoMarkdown("leve", italico = true),
            ),
            interpretarMarkdown("__forte__ e _leve_"),
        )
    }

    @Test
    fun `negrito e italico juntos`() {
        assertEquals(
            listOf(TrechoMarkdown("importante", negrito = true, italico = true)),
            interpretarMarkdown("***importante***"),
        )
    }

    @Test
    fun `sublinhado dentro de palavra nao vira italico`() {
        assertEquals("use product_id e estoque_minimo", texto("use product_id e estoque_minimo"))
    }

    @Test
    fun `asterisco solto fica como texto`() {
        assertEquals("2 * 3 * 4", texto("2 * 3 * 4"))
    }

    @Test
    fun `codigo inline vira trecho de codigo`() {
        assertEquals(
            listOf(TrechoMarkdown("Use "), TrechoMarkdown("estoque", codigo = true), TrechoMarkdown(".")),
            interpretarMarkdown("Use `estoque`."),
        )
    }

    @Test
    fun `listas com marcador e numeradas`() {
        assertEquals(
            "•\u00A0Arroz\n•\u00A0Feijão\n1.\u00A0Primeiro\n2.\u00A0Segundo",
            texto("- Arroz\n* Feijão\n1. Primeiro\n2. Segundo"),
        )
    }

    @Test
    fun `lista aninhada ganha recuo`() {
        assertEquals("•\u00A0Grãos\n    •\u00A0Arroz", texto("- Grãos\n  - Arroz"))
    }

    @Test
    fun `item de lista com negrito`() {
        assertEquals(
            listOf(
                TrechoMarkdown("•\u00A0"),
                TrechoMarkdown("Feijão", negrito = true),
                TrechoMarkdown(": 12 kg"),
            ),
            interpretarMarkdown("- **Feijão**: 12 kg"),
        )
    }

    @Test
    fun `titulo vira negrito sem as hashtags`() {
        assertEquals(
            listOf(TrechoMarkdown("Estoque da cozinha", negrito = true)),
            interpretarMarkdown("## Estoque da cozinha"),
        )
    }

    @Test
    fun `citacao vira italico`() {
        assertEquals(listOf(TrechoMarkdown("atenção", italico = true)), interpretarMarkdown("> atenção"))
    }

    @Test
    fun `tabela vira linhas simples com cabecalho em negrito`() {
        val markdown = "| Produto | Qtd |\n|---|---|\n| Arroz | 25 kg |"
        assertEquals(
            listOf(
                TrechoMarkdown("Produto", negrito = true),
                TrechoMarkdown("  ·  "),
                TrechoMarkdown("Qtd", negrito = true),
                TrechoMarkdown("\nArroz  ·  25 kg"),
            ),
            interpretarMarkdown(markdown),
        )
    }

    @Test
    fun `bloco de codigo preserva o conteudo e some com as cercas`() {
        assertEquals(
            listOf(TrechoMarkdown("antes\n"), TrechoMarkdown("a = 1", codigo = true), TrechoMarkdown("\ndepois")),
            interpretarMarkdown("antes\n```\na = 1\n```\ndepois"),
        )
    }

    @Test
    fun `regra horizontal some e linhas em branco repetidas viram uma`() {
        assertEquals("a\n\nb", texto("a\n\n\n---\n\nb"))
    }

    @Test
    fun `link mostra so o texto`() {
        assertEquals("veja o guia", texto("veja [o guia](https://exemplo.com)"))
    }

    @Test
    fun `marcador escapado vira caractere literal`() {
        assertEquals("*não é itálico*", texto("\\*não é itálico\\*"))
    }

    @Test
    fun `quebras de linha do windows sao normalizadas`() {
        assertEquals("a\nb", texto("a\r\nb"))
    }
}
