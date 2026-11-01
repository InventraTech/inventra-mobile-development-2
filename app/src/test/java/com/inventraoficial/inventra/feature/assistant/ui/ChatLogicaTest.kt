package com.inventraoficial.inventra.feature.assistant.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

class ChatLogicaTest {
    private val zona: ZoneId = ZoneOffset.UTC
    private val hoje: LocalDate = LocalDate.of(2026, 10, 22)

    private fun conversa(
        id: String,
        dia: LocalDate,
    ) = ConversaResumo(
        id = id,
        titulo = id,
        iniciadaEmMillis = dia.atTime(12, 0).toInstant(ZoneOffset.UTC).toEpochMilli(),
    )

    // ---------- título ----------

    @Test
    fun `titulo usa a primeira frase do resumo sem o sujeito`() {
        val titulo = tituloDaConversa("O usuário perguntou sobre lotes vencendo. Depois pediu uma requisição.")

        assertEquals("Perguntou sobre lotes vencendo", titulo)
    }

    @Test
    fun `titulo longo e cortado com reticencias`() {
        val titulo =
            tituloDaConversa("Consulta detalhada sobre todos os produtos com validade próxima do vencimento neste mês")

        assertTrue(titulo.length <= 48)
        assertTrue(titulo.endsWith("…"))
    }

    @Test
    fun `titulo remove marcas de markdown e tem fallback`() {
        assertEquals("Estoque de arroz", tituloDaConversa("**Estoque** de _arroz_"))
        assertEquals("Conversa sem título", tituloDaConversa("   "))
    }

    // ---------- datas ----------

    @Test
    fun `data da IA e lida com Z, com fuso e sem sufixo`() {
        val esperado = Instant.parse("2026-10-22T14:05:11Z")

        assertEquals(esperado, interpretarDataIso("2026-10-22T14:05:11Z"))
        assertEquals(esperado, interpretarDataIso("2026-10-22T14:05:11+00:00"))
        assertEquals(esperado, interpretarDataIso("2026-10-22T14:05:11"))
        assertEquals(esperado.plusMillis(123), interpretarDataIso("2026-10-22T14:05:11.123000"))
        assertNull(interpretarDataIso("ontem"))
    }

    @Test
    fun `conversas sao agrupadas como nas IAs grandes`() {
        val conversas =
            listOf(
                conversa("antiga", hoje.minusDays(90)),
                conversa("hoje", hoje),
                conversa("mes", hoje.minusDays(20)),
                conversa("ontem", hoje.minusDays(1)),
                conversa("semana", hoje.minusDays(5)),
            )

        val grupos = agruparPorData(conversas, hoje, zona)

        assertEquals(
            listOf("Hoje", "Ontem", "Últimos 7 dias", "Últimos 30 dias", "Mais antigas"),
            grupos.map { it.titulo },
        )
        assertEquals(listOf("hoje"), grupos[0].conversas.map { it.id })
        assertEquals(listOf("antiga"), grupos[4].conversas.map { it.id })
    }

    @Test
    fun `dentro do grupo a conversa mais recente vem primeiro e grupos vazios nao aparecem`() {
        val conversas =
            listOf(
                conversa("a", hoje.minusDays(3)),
                conversa("b", hoje.minusDays(2)),
            )

        val grupos = agruparPorData(conversas, hoje, zona)

        assertEquals(1, grupos.size)
        assertEquals("Últimos 7 dias", grupos[0].titulo)
        assertEquals(listOf("b", "a"), grupos[0].conversas.map { it.id })
    }

    // ---------- fluxo da resposta ----------

    @Test
    fun `tool aparece sob o no em que rodou, na ordem em que chegou`() {
        val passos =
            listOf(
                PassoIa("n1", "Entendi o pedido", true),
                PassoIa("n2", "Analisando o estoque…", false),
                PassoIa("t1", "Planejando a baixa por lote…", true, pai = "n2"),
                PassoIa("t2", "Preparando a proposta…", false, pai = "n2"),
            )

        val grupos = agruparPassos(passos)

        assertEquals(listOf("n1", "n2"), grupos.map { it.passo.id })
        assertEquals(emptyList<PassoIa>(), grupos[0].filhos)
        assertEquals(listOf("t1", "t2"), grupos[1].filhos.map { it.id })
    }

    @Test
    fun `passo cujo pai nao existe fica no primeiro nivel e nada some`() {
        val passos = listOf(PassoIa("t1", "Consultando…", true, pai = "nao-existe"))

        assertEquals(listOf("t1"), agruparPassos(passos).map { it.passo.id })
    }

    @Test
    fun `previa do pensamento e a primeira linha sem marcas de markdown`() {
        val pensamento = "**Calculando o total**\n\nVou somar os lotes ativos."

        assertEquals("Calculando o total", previaDoPensamento(pensamento))
        assertEquals("", previaDoPensamento("  \n "))
        assertTrue(previaDoPensamento("a".repeat(200)).length <= 70)
        assertTrue(previaDoPensamento("a".repeat(200)).endsWith("…"))
    }

    // ---------- voz ----------

    @Test
    fun `volume da voz vai de 0 no silencio a 1 na voz alta`() {
        assertEquals(0f, nivelDaVoz(-2f), 0.001f)
        assertEquals(0f, nivelDaVoz(-10f), 0.001f)
        assertEquals(0.5f, nivelDaVoz(4f), 0.001f)
        assertEquals(1f, nivelDaVoz(10f), 0.001f)
        assertEquals(1f, nivelDaVoz(30f), 0.001f)
    }

    @Test
    fun `amplitude do microfone vira volume sem passar de 1`() {
        assertEquals(0f, nivelDoMicrofone(0), 0.001f)
        assertEquals(1f, nivelDoMicrofone(32_767), 0.001f)
        assertEquals(1f, nivelDoMicrofone(99_999), 0.001f)
        // A raiz deixa a voz baixa mais visível: 25% da amplitude vira 50% do volume.
        assertEquals(0.5f, nivelDoMicrofone(8_192), 0.01f)
    }

    @Test
    fun `transcricao junta os trechos com um espaco so`() {
        assertEquals("oi tudo bem", juntarTranscricao("oi ", " tudo bem"))
        assertEquals("oi", juntarTranscricao("oi", ""))
        assertEquals("tudo bem", juntarTranscricao("", "tudo bem"))
        assertEquals("", juntarTranscricao("  ", ""))
    }

    @Test
    fun `fala longa mostra so o fim com reticencias`() {
        assertEquals("quanto tenho de arroz", fimDaTranscricao("quanto tenho de arroz"))
        val longa = "palavra ".repeat(40)

        val visivel = fimDaTranscricao(longa, maximo = 30)

        assertTrue(visivel.startsWith("…"))
        assertTrue(visivel.length <= 31)
        assertTrue(longa.trim().endsWith(visivel.removePrefix("…").trim()))
    }

    // ---------- tempo da resposta ----------

    @Test
    fun `duracao fica em segundos ate um minuto e depois em minutos e segundos`() {
        assertEquals("0 s", formatarDuracao(0))
        assertEquals("8 s", formatarDuracao(8))
        assertEquals("59 s", formatarDuracao(59))
        assertEquals("1 min 00 s", formatarDuracao(60))
        assertEquals("1 min 05 s", formatarDuracao(65))
        assertEquals("12 min 30 s", formatarDuracao(750))
        assertEquals("0 s", formatarDuracao(-3))
    }
}
