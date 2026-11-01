package com.inventraoficial.inventra.feature.assistant.ui

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatArtefatosTest {
    private fun ler(json: String) = lerArtefato(Json.parseToJsonElement(json))

    private val tabelaJson =
        """
        {"type":"table","source":"spring","mocked":true,
         "columns":[{"key":"produto","label":"Produto"},{"key":"quantidade","label":"Quantidade atual"},
                    {"key":"validade","label":"Validade"}],
         "rows":[{"produto":"Arroz","quantidade":12,"validade":"2026-11-05"},
                 {"produto":"Feijão","quantidade":2.5,"validade":null}]}
        """.trimIndent()

    @Test
    fun `tabela vira linhas de texto com data brasileira, virgula decimal e traco para vazio`() {
        val tabela = ler(tabelaJson) as Artefato.Tabela

        assertEquals(listOf("Produto", "Quantidade atual", "Validade"), tabela.colunas.map { it.titulo })
        assertEquals(listOf("Arroz", "12", "05/11/2026"), tabela.linhas[0])
        assertEquals(listOf("Feijão", "2,5", "—"), tabela.linhas[1])
        assertTrue(tabela.simulado)
    }

    @Test
    fun `nota da IA sobre como ler os dados e guardada, e vazia ou ausente vira nulo`() {
        val comNota =
            ler(
                """
                {"type":"chart","mocked":false,"chart_type":"bar","labels":["A","B"],
                 "datasets":[{"label":"x","data":[1,2]}],"unit":"kg",
                 "note":"Só os itens em kg entram no gráfico; 2 itens ficaram de fora."}
                """.trimIndent(),
            ) as Artefato.Grafico
        val nula = ler(tabelaJson) as Artefato.Tabela
        val vazia =
            ler(
                """
                {"type":"table","mocked":false,"columns":[{"key":"a","label":"A"}],
                 "rows":[{"a":1}],"note":"  "}
                """.trimIndent(),
            ) as Artefato.Tabela

        assertEquals("Só os itens em kg entram no gráfico; 2 itens ficaram de fora.", comNota.nota)
        assertNull(nula.nota)
        assertNull(vazia.nota)
    }

    @Test
    fun `grafico de barras com duas series guarda unidade e valores`() {
        val grafico =
            ler(
                """
                {"type":"chart","source":"spring","mocked":false,"chart_type":"bar",
                 "labels":["Arroz","Feijão"],
                 "datasets":[{"label":"Quantidade atual","data":[12,3.5]},{"label":"Estoque mínimo","data":[10,6]}],
                 "unit":"kg"}
                """.trimIndent(),
            ) as Artefato.Grafico

        assertEquals(TipoDeGrafico.BARRAS, grafico.tipo)
        assertEquals(listOf("Arroz", "Feijão"), grafico.categorias)
        assertEquals(listOf(12.0, 3.5), grafico.series[0].valores)
        assertEquals("Estoque mínimo", grafico.series[1].nome)
        assertEquals("kg", grafico.unidade)
        assertFalse(grafico.simulado)
    }

    @Test
    fun `grafico de pizza e lido como pizza`() {
        val grafico =
            ler(
                """
                {"type":"chart","source":"spring","mocked":false,"chart_type":"pie",
                 "labels":["DRAFT","APPROVED"],
                 "datasets":[{"label":"Quantidade de registros","data":[3,1]}],"unit":"registros"}
                """.trimIndent(),
            ) as Artefato.Grafico

        assertEquals(TipoDeGrafico.PIZZA, grafico.tipo)
        assertEquals("registros", grafico.unidade)
    }

    @Test
    fun `artefato de tipo desconhecido, incompleto ou incoerente e ignorado`() {
        assertNull(ler("""{"type":"mapa","mocked":false}"""))
        assertNull(ler("""{"type":"table","mocked":false}"""))
        assertNull(ler("""{"type":"chart","mocked":false,"chart_type":"radar","labels":["a"],"datasets":[]}"""))
        assertNull(
            ler(
                """
                {"type":"chart","mocked":false,"chart_type":"bar","labels":["a","b"],
                 "datasets":[{"label":"x","data":[1]}]}
                """.trimIndent(),
            ),
        )
        assertNull(lerArtefato(Json.parseToJsonElement("\"texto solto\"")))
    }

    @Test
    fun `lerArtefatos mantem os bons e descarta os que nao entende`() {
        val lista = listOf(Json.parseToJsonElement("""{"type":"mapa"}"""), Json.parseToJsonElement(tabelaJson))

        val artefatos = lerArtefatos(lista)

        assertEquals(1, artefatos.size)
        assertTrue(artefatos[0] is Artefato.Tabela)
    }

    @Test
    fun `numeros usam ponto de milhar e virgula decimal`() {
        assertEquals("1.234,5", formatarNumero(1234.5))
        assertEquals("12", formatarNumero(12.0))
        assertEquals("0,33", formatarNumero(0.3333))
        assertEquals("12 kg", formatarValor(12.0, "kg"))
        assertEquals("12", formatarValor(12.0, null))
    }

    @Test
    fun `coluna so com numeros alinha a direita`() {
        val tabela = ler(tabelaJson) as Artefato.Tabela

        assertFalse(colunaEhNumerica(tabela, 0))
        assertTrue(colunaEhNumerica(tabela, 1))
        assertFalse(colunaEhNumerica(tabela, 2))
    }

    @Test
    fun `pizza com muitas categorias junta as menores em Outros`() {
        val nomes = listOf("a", "b", "c", "d", "e", "f", "g", "h")
        val valores = listOf(1.0, 8.0, 2.0, 7.0, 3.0, 6.0, 4.0, 5.0)

        val fatias = agruparFatias(nomes, valores, maximo = 6)

        assertEquals(listOf("b", "d", "f", "h", "g", "Outros"), fatias.map { it.nome })
        assertEquals(1.0 + 2.0 + 3.0, fatias.last().valor, 0.0001)
        assertEquals(valores.sum(), fatias.sumOf { it.valor }, 0.0001)
    }

    @Test
    fun `categorias dentro do limite ficam como vieram`() {
        val fatias = agruparFatias(listOf("x", "y"), listOf(1.0, 2.0), maximo = 6)

        assertEquals(listOf("x", "y"), fatias.map { it.nome })
    }

    @Test
    fun `percentual sem casas, com menor que 1 e total zero`() {
        assertEquals("33%", percentualLegivel(1.0, 3.0))
        assertEquals("<1%", percentualLegivel(0.1, 100.0))
        assertEquals("100%", percentualLegivel(5.0, 5.0))
        assertEquals("0%", percentualLegivel(1.0, 0.0))
    }

    @Test
    fun `descricao para leitor de tela cita o tipo, a quantidade e os primeiros itens`() {
        val grafico =
            ler(
                """
                {"type":"chart","mocked":false,"chart_type":"pie","labels":["A","B"],
                 "datasets":[{"label":"x","data":[3,1]}],"unit":"registros"}
                """.trimIndent(),
            ) as Artefato.Grafico
        val tabela = ler(tabelaJson) as Artefato.Tabela

        assertEquals("Gráfico de pizza com 2 itens: A 3 registros; B 1 registros.", descricaoDoArtefato(grafico))
        assertEquals(
            "Tabela com 2 linhas e as colunas Produto, Quantidade atual, Validade.",
            descricaoDoArtefato(tabela),
        )
    }
}
