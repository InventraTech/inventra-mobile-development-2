package com.inventraoficial.inventra.feature.assistant.ui

import com.inventraoficial.inventra.data.remote.api.EventoIa
import com.inventraoficial.inventra.data.remote.api.interpretarEventoIa
import com.inventraoficial.inventra.data.repository.FakeChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ChatStreamingTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val dispatcher = StandardTestDispatcher()
    private val fake = FakeChatRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun perguntar(): ChatViewModel {
        val viewModel = ChatViewModel(chatRepository = fake, store = ChatSessionStore())
        viewModel.onInputChange("quanto tenho de arroz?")
        viewModel.onSend()
        dispatcher.scheduler.advanceUntilIdle()
        return viewModel
    }

    // ---------- linhas do stream ----------

    @Test
    fun `cada tipo de linha vira o evento certo`() {
        assertEquals(
            EventoIa.Etapa("t1", concluida = false, texto = "Consultando o estoque…"),
            interpretarEventoIa("""{"type":"etapa","id":"t1","status":"inicio","texto":"Consultando o estoque…"}""", json),
        )
        assertEquals(
            EventoIa.Etapa("t1", concluida = true, texto = null),
            interpretarEventoIa("""{"type":"etapa","id":"t1","status":"fim"}""", json),
        )
        assertEquals(
            EventoIa.Pensamento("estoquista", "Vou somar os lotes."),
            interpretarEventoIa("""{"type":"pensamento","id":"estoquista","delta":"Vou somar os lotes."}""", json),
        )
        assertEquals(EventoIa.Texto("Você tem "), interpretarEventoIa("""{"type":"texto","delta":"Você tem "}""", json))
        assertEquals(EventoIa.Fim(false), interpretarEventoIa("""{"type":"fim","status":"aprovado"}""", json))
        assertEquals(EventoIa.Fim(true), interpretarEventoIa("""{"type":"fim","status":"segura"}""", json))
        assertEquals(EventoIa.Erro("falhou"), interpretarEventoIa("""{"type":"erro","mensagem":"falhou"}""", json))
    }

    @Test
    fun `artefato e cartao carregam o mesmo json do chat normal`() {
        val artefato =
            interpretarEventoIa(
                """{"type":"artefato","artifact":{"type":"table","columns":[],"rows":[]}}""",
                json,
            )
        assertTrue(artefato is EventoIa.ArtefatoPronto)

        val cartao =
            interpretarEventoIa(
                """{"type":"cartao","approval_id":"a1","pending_action":{"id":"a1","proposal_hash":"h","operation":"CONSUME"}}""",
                json,
            )
        assertEquals("a1", (cartao as EventoIa.AcaoProposta).acao.id)
    }

    @Test
    fun `tipo desconhecido e linha quebrada sao ignorados sem derrubar a conversa`() {
        assertNull(interpretarEventoIa("""{"type":"novidade","x":1}""", json))
        assertNull(interpretarEventoIa("isso nao e json", json))
        assertNull(interpretarEventoIa("""{"type":"texto","delta":""}""", json))
    }

    // ---------- bolha que cresce ----------

    @Test
    fun `passo gira ate terminar e pensamento e texto ficam em campos separados`() {
        val resposta = RespostaEmStream()

        resposta.aplicar(EventoIa.Etapa("t1", concluida = false, texto = "Consultando o estoque…"))
        assertEquals(listOf(PassoIa("t1", "Consultando o estoque…", false)), resposta.paraMensagem("m").passos)

        resposta.aplicar(EventoIa.Pensamento("estoquista", "Preciso somar. "))
        resposta.aplicar(EventoIa.Pensamento("estoquista", "Depois ordenar."))
        resposta.aplicar(EventoIa.Etapa("t1", concluida = true, texto = "Estoque consultado"))
        resposta.aplicar(EventoIa.Texto("Você tem "))
        resposta.aplicar(EventoIa.Texto("12 produtos."))

        val mensagem = resposta.paraMensagem("m")
        assertEquals(listOf(PassoIa("t1", "Estoque consultado", true)), mensagem.passos)
        assertEquals("Preciso somar. Depois ordenar.", mensagem.pensamento)
        assertEquals("Você tem 12 produtos.", mensagem.conteudo)
        assertFalse(mensagem.ehDoUsuario)
    }

    @Test
    fun `pensamentos de especialistas diferentes ficam em paragrafos separados`() {
        val resposta = RespostaEmStream()

        resposta.aplicar(EventoIa.Pensamento("estoquista", "Olhando lotes."))
        resposta.aplicar(EventoIa.Pensamento("comprador", "Olhando pedidos."))

        assertEquals("Olhando lotes.\n\nOlhando pedidos.", resposta.paraMensagem("m").pensamento)
    }

    @Test
    fun `antes do primeiro evento nao ha conteudo e o fim nao muda a bolha`() {
        val resposta = RespostaEmStream()
        assertFalse(resposta.temConteudo)

        assertFalse(resposta.aplicar(EventoIa.Fim(respostaSegura = true)))

        assertTrue(resposta.terminou)
        assertTrue(resposta.respostaSegura)
    }

    @Test
    fun `conexao que cai no meio nao deixa passo girando`() {
        val resposta = RespostaEmStream()
        resposta.aplicar(EventoIa.Etapa("t1", concluida = false, texto = "Consultando…"))

        resposta.encerrarPassosPendentes()

        assertTrue(resposta.paraMensagem("m").passos.single().concluido)
    }

    // ---------- ViewModel ----------

    @Test
    fun `resposta em stream aparece com passos pensamento e texto e nao digita de novo`() {
        fake.eventosDoStream =
            listOf(
                EventoIa.Etapa("t1", concluida = false, texto = "Consultando o estoque…"),
                EventoIa.Pensamento("estoquista", "Vou somar os lotes."),
                EventoIa.Etapa("t1", concluida = true, texto = "Estoque consultado"),
                EventoIa.Texto("Você tem "),
                EventoIa.Texto("30 kg."),
                EventoIa.Fim(respostaSegura = false),
            )

        val estado = perguntar().uiState.value

        val resposta = estado.mensagens.last()
        assertEquals("Você tem 30 kg.", resposta.conteudo)
        assertEquals("Vou somar os lotes.", resposta.pensamento)
        assertTrue(resposta.passos.single().concluido)
        assertEquals(2, estado.mensagens.size)
        assertFalse(estado.carregando)
        assertEquals(1, estado.respostasDaIa)
        assertNull(estado.mensagemAnimandoId)
        assertNull(estado.erro)
    }

    @Test
    fun `servidor sem stream entrega a resposta inteira e ela digita como antes`() {
        fake.eventosDoStream = null
        fake.respostaPadrao = "resposta inteira"

        val estado = perguntar().uiState.value

        assertEquals("resposta inteira", estado.mensagens.last().conteudo)
        assertNotNull(estado.mensagemAnimandoId)
        assertFalse(estado.carregando)
    }

    @Test
    fun `evento de erro vira mensagem de erro e libera o campo`() {
        fake.eventosDoStream =
            listOf(
                EventoIa.Etapa("t1", concluida = false, texto = "Consultando…"),
                EventoIa.Erro("A IA caiu no meio."),
            )

        val estado = perguntar().uiState.value

        assertEquals("A IA caiu no meio.", estado.erro)
        assertFalse(estado.carregando)
        // O passo que girava não fica girando depois da falha.
        assertTrue(estado.mensagens.first { !it.ehDoUsuario && it.passos.isNotEmpty() }.passos.single().concluido)
    }

    @Test
    fun `stream que acaba sem fim e tratado como resposta interrompida`() {
        fake.eventosDoStream = listOf(EventoIa.Texto("Você tem "))

        val estado = perguntar().uiState.value

        assertNotNull(estado.erro)
        assertFalse(estado.carregando)
        assertEquals(0, estado.respostasDaIa)
    }

    @Test
    fun `conexao que quebra no meio mostra o erro e libera o campo`() {
        fake.eventosDoStream = listOf(EventoIa.Texto("Você tem "))
        fake.erroNoMeioDoStream = IOException("conexão perdida")

        val estado = perguntar().uiState.value

        assertTrue(estado.erro.orEmpty().contains("conexão perdida"))
        assertFalse(estado.carregando)
    }

    @Test
    fun `etapa de tool traz o pai e o comando que ela executou`() {
        val evento =
            interpretarEventoIa(
                """{"type":"etapa","id":"t1","status":"inicio","texto":"Planejando a baixa por lote…","pai":"n2","detalhe":"planejar_baixa(product_name='chá', quantity='3')"}""",
                json,
            )

        assertEquals(
            EventoIa.Etapa(
                id = "t1",
                concluida = false,
                texto = "Planejando a baixa por lote…",
                pai = "n2",
                detalhe = "planejar_baixa(product_name='chá', quantity='3')",
            ),
            evento,
        )
    }

    @Test
    fun `fim da etapa nao perde o pai nem o comando do inicio`() {
        val resposta = RespostaEmStream()
        resposta.aplicar(EventoIa.Etapa("t1", concluida = false, texto = "Planejando…", pai = "n2", detalhe = "planejar_baixa()"))

        resposta.aplicar(EventoIa.Etapa("t1", concluida = true, texto = "Baixa planejada"))

        assertEquals(
            listOf(PassoIa("t1", "Baixa planejada", true, pai = "n2", detalhe = "planejar_baixa()")),
            resposta.paraMensagem("m").passos,
        )
    }
}
