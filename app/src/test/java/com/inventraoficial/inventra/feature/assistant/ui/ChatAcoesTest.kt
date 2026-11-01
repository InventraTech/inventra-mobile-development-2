package com.inventraoficial.inventra.feature.assistant.ui

import com.inventraoficial.inventra.data.remote.api.AcaoPendenteDto
import com.inventraoficial.inventra.data.remote.api.AprovacaoDto
import com.inventraoficial.inventra.data.remote.api.ResultadoExecucaoDto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class ChatAcoesTest {
    private fun objeto(json: String): JsonObject = Json.parseToJsonElement(json).jsonObject

    private fun proposta(
        operacao: String = "registrar_saida",
        parametros: String = """{"batchId":1,"quantity":"10"}""",
        antes: List<String> =
            listOf(
                """
                {"parameter":"batchId","record":{"id":1,"batchNumber":"L-10",
                 "product":{"name":"Arroz","unit":{"symbol":"kg"}}}}
                """.trimIndent(),
            ),
    ) = AcaoPendenteDto(
        id = "acao-1",
        proposal_hash = "abc123",
        operation = operacao,
        parameters = objeto(parametros),
        justification = "Consumo informado pelo usuário",
        status = "AGUARDANDO_APROVACAO",
        expires_at = "2026-11-03T08:08:57.385732Z",
        before = antes.map { objeto(it) },
    )

    private fun cartao(estado: EstadoDaAcao = EstadoDaAcao.AGUARDANDO) = proposta().paraAcao().copy(estado = estado)

    @Test
    fun `proposta vira cartao com titulo, nomes dos registros e unidade`() {
        val acao = proposta().paraAcao()

        assertEquals("acao-1", acao.id)
        assertEquals("abc123", acao.propostaHash)
        assertEquals("Registrar saída de estoque", acao.titulo)
        assertEquals(
            listOf(
                LinhaDaAcao("Lote", "Arroz, lote L-10 (nº 1)"),
                LinhaDaAcao("Quantidade", "10 kg"),
            ),
            acao.linhas,
        )
        assertEquals("Consumo informado pelo usuário", acao.justificativa)
        assertEquals(EstadoDaAcao.AGUARDANDO, acao.estado)
        assertEquals(Instant.parse("2026-11-03T08:08:57.385732Z").toEpochMilli(), acao.expiraEmMillis)
    }

    @Test
    fun `sem registro anterior mostra so o numero e a quantidade sem unidade`() {
        val acao = proposta(antes = emptyList()).paraAcao()

        assertEquals(listOf(LinhaDaAcao("Lote", "nº 1"), LinhaDaAcao("Quantidade", "10")), acao.linhas)
    }

    @Test
    fun `numero do registro nao ganha ponto de milhar`() {
        val acao = proposta(parametros = """{"batchId":1234}""", antes = emptyList()).paraAcao()

        assertEquals(listOf(LinhaDaAcao("Lote", "nº 1234")), acao.linhas)
    }

    @Test
    fun `decimal vem com virgula, tipo e datas viram texto legivel`() {
        val acao =
            proposta(
                operacao = "criar_requisicao",
                parametros = """{"type":"PURCHASE","origin":"app","estimatedPrice":"4.50","entryDate":"2026-11-05"}""",
                antes = emptyList(),
            ).paraAcao()

        assertEquals("Criar requisição", acao.titulo)
        assertEquals(
            listOf(
                LinhaDaAcao("Tipo", "Compra"),
                LinhaDaAcao("Origem", "app"),
                LinhaDaAcao("Preço estimado", "4,50"),
                LinhaDaAcao("Entrada", "05/11/2026"),
            ),
            acao.linhas,
        )
    }

    @Test
    fun `operacao desconhecida ganha um titulo legivel`() {
        val acao = proposta(operacao = "mover_estoque", parametros = "{}", antes = emptyList()).paraAcao()

        assertEquals("Mover estoque", acao.titulo)
        assertTrue(acao.linhas.isEmpty())
    }

    @Test
    fun `estados do servidor viram estados do cartao`() {
        assertEquals(EstadoDaAcao.AGUARDANDO, estadoDoServidor("AGUARDANDO_APROVACAO"))
        assertEquals(EstadoDaAcao.APROVADA, estadoDoServidor("APROVADA_PENDENTE_INTEGRACAO"))
        assertEquals(EstadoDaAcao.APROVADA, estadoDoServidor("EM_EXECUCAO"))
        assertEquals(EstadoDaAcao.CONCLUIDA, estadoDoServidor("CONCLUIDA"))
        assertEquals(EstadoDaAcao.RECUSADA, estadoDoServidor("RECUSADA"))
        assertEquals(EstadoDaAcao.FALHOU, estadoDoServidor("FALHOU"))
        assertEquals(EstadoDaAcao.EXPIRADA, estadoDoServidor("EXPIRADA"))
        assertEquals(EstadoDaAcao.INCERTA, estadoDoServidor("RESULTADO_INCERTO"))
        assertNull(estadoDoServidor("QUALQUER_OUTRO"))
    }

    @Test
    fun `resposta concluida mostra a mensagem do sistema e marca simulacao`() {
        val resposta =
            AprovacaoDto(
                status = "CONCLUIDA",
                result = ResultadoExecucaoDto(ok = true, code = "OK", message = "Saída registrada.", mocked = true),
            )

        val acao = resposta.aplicarEm(cartao(EstadoDaAcao.APROVANDO))

        assertEquals(EstadoDaAcao.CONCLUIDA, acao.estado)
        assertNull(acao.detalhe)
        assertTrue(acao.simulado)
    }

    @Test
    fun `falha mostra a mensagem do sistema ou o erro de execucao`() {
        val comMensagem =
            AprovacaoDto(status = "FALHOU", result = ResultadoExecucaoDto(message = "Estoque insuficiente."))
        val soErro = AprovacaoDto(status = "FALHOU", execution_error = "CONFLITO")

        assertEquals("Estoque insuficiente.", comMensagem.aplicarEm(cartao()).detalhe)
        assertEquals("CONFLITO", soErro.aplicarEm(cartao()).detalhe)
        assertEquals(EstadoDaAcao.FALHOU, soErro.aplicarEm(cartao()).estado)
    }

    @Test
    fun `status desconhecido nunca reabre os botoes`() {
        val acao = AprovacaoDto(status = "ALGO_NOVO").aplicarEm(cartao())

        assertEquals(EstadoDaAcao.FALHOU, acao.estado)
    }

    @Test
    fun `resultado incerto avisa para conferir antes de repetir`() {
        val acao = AprovacaoDto(status = "RESULTADO_INCERTO").aplicarEm(cartao())

        assertEquals(EstadoDaAcao.INCERTA, acao.estado)
        assertTrue(acao.detalhe.orEmpty().contains("Confira no sistema"))
    }

    @Test
    fun `recusada e aprovada pendente nao trazem erro`() {
        assertNull(AprovacaoDto(status = "RECUSADA").aplicarEm(cartao()).detalhe)
        val aprovada = AprovacaoDto(status = "APROVADA_PENDENTE_INTEGRACAO").aplicarEm(cartao())
        assertEquals(EstadoDaAcao.APROVADA, aprovada.estado)
    }

    @Test
    fun `cartao expira pelo relogio mas so enquanto aguarda`() {
        val acao = cartao()
        val depois = acao.expiraEmMillis!! + 1

        assertTrue(acao.podeDecidir(depois - 2))
        assertTrue(acao.expirou(depois))
        assertFalse(acao.podeDecidir(depois))
        assertFalse(cartao(EstadoDaAcao.CONCLUIDA).expirou(depois))
        assertFalse(acao.copy(expiraEmMillis = null).expirou(Long.MAX_VALUE))
    }

    @Test
    fun `prazo aparece com dia e hora`() {
        val millis = Instant.parse("2026-11-03T08:08:00Z").toEpochMilli()

        assertEquals("03/11 às 08:08", prazoLegivel(millis, ZoneOffset.UTC))
        assertEquals("03/11 às 05:08", prazoLegivel(millis, ZoneOffset.ofHours(-3)))
    }

    @Test
    fun `texto da IA perde as propostas repetidas, o estado antigo e o aviso de nada alterado`() {
        val texto =
            """
            **Registrar saída de estoque** — proposta `acao-1`
            - Lote: Arroz (nº 1)
            - Quantidade: 10
            - Estado: AGUARDANDO_APROVACAO — aguardando sua confirmação

            **Registrar saída de estoque** — proposta `acao-2`
            - Lote: Feijão (nº 2)
            - Quantidade: 3
            - Estado: AGUARDANDO_APROVACAO — aguardando sua confirmação
            Somente as propostas listadas foram registradas; outros itens não foram propostos.

            Nada foi alterado nesta conversa. Revise cada proposta e confirme ou rejeite com sua própria identidade.
            """.trimIndent()

        val limpo = limparTextoDasPropostas(texto)

        assertEquals("", limpo)
    }

    @Test
    fun `etapas que falharam continuam no texto porque o cartao nao as mostra`() {
        val texto =
            """
            **Registrar saída de estoque** — proposta `acao-1`
            - Lote: Arroz (nº 1)
            - Estado: AGUARDANDO_APROVACAO — aguardando sua confirmação
            Somente as propostas listadas foram registradas; outros itens não foram propostos.

            Nada foi alterado nesta conversa. Revise cada proposta.

            Etapas não concluídas — Não consegui concluir:
            - CONFLITO: Lote não ativo ou saldo insuficiente.
            """.trimIndent()

        val limpo = limparTextoDasPropostas(texto)

        assertEquals(
            "Etapas não concluídas — Não consegui concluir:\n- CONFLITO: Lote não ativo ou saldo insuficiente.",
            limpo,
        )
    }

    @Test
    fun `resumo das propostas concorda com a quantidade`() {
        assertTrue(resumoDasPropostas(1).startsWith("Preparei uma proposta para você revisar."))
        assertTrue(resumoDasPropostas(3).startsWith("Preparei 3 propostas para você revisar."))
        assertTrue(resumoDasPropostas(2).contains("**Aprovar**"))
    }

    @Test
    fun `acao e atualizada na conversa que aparece e na guardada atras do historico`() {
        val aoVivo = Mensagem(id = "m1", conteudo = "ia", ehDoUsuario = false, acoes = listOf(cartao()))
        val estado =
            ChatUiState(
                mensagens = listOf(Mensagem(id = "h1", conteudo = "antiga", ehDoUsuario = true)),
                conversaVisualizadaId = "d1",
                conversaEmAndamento = ChatUiState(mensagens = listOf(aoVivo)),
            )

        assertNotNull(estado.acaoPorId("acao-1"))
        assertNull(estado.acaoPorId("outra"))

        val atualizado = estado.comAcaoAtualizada("acao-1") { it.copy(estado = EstadoDaAcao.CONCLUIDA) }

        assertEquals(
            EstadoDaAcao.CONCLUIDA,
            atualizado.conversaEmAndamento
                ?.mensagens
                ?.first()
                ?.acoes
                ?.first()
                ?.estado,
        )
        assertEquals(estado.mensagens, atualizado.mensagens)
    }

    private fun loteComSaldo(saldo: String) =
        listOf(
            """
            {"parameter":"batchId","record":{"id":1,"batchNumber":"L-10","currentQuantity":$saldo,
             "product":{"name":"Arroz","unit":{"symbol":"kg"}}}}
            """.trimIndent(),
        )

    @Test
    fun `saida mostra o saldo do lote antes e depois`() {
        val acao = proposta(parametros = """{"batchId":1,"quantity":"10"}""", antes = loteComSaldo("85")).paraAcao()

        assertEquals(MudancaDeSaldo(antes = "85 kg", depois = "75 kg"), acao.mudanca)
    }

    @Test
    fun `saldo decimal sai com virgula e sem zeros sobrando`() {
        val acao = proposta(parametros = """{"batchId":1,"quantity":"2.5"}""", antes = loteComSaldo("10.000")).paraAcao()

        assertEquals(MudancaDeSaldo(antes = "10 kg", depois = "7,5 kg"), acao.mudanca)
    }

    @Test
    fun `entrada soma e ajuste define a nova quantidade`() {
        val entrada =
            proposta(operacao = "registrar_entrada", parametros = """{"batchId":1,"quantity":"5"}""", antes = loteComSaldo("20"))
        val ajuste =
            proposta(operacao = "ajustar_estoque", parametros = """{"batchId":1,"newQuantity":"12"}""", antes = loteComSaldo("20"))

        assertEquals("25 kg", entrada.paraAcao().mudanca?.depois)
        assertEquals("12 kg", ajuste.paraAcao().mudanca?.depois)
    }

    @Test
    fun `sem saldo no snapshot ou em outra operacao nao ha antes e depois`() {
        assertNull(proposta().paraAcao().mudanca)
        assertNull(
            proposta(operacao = "criar_requisicao", parametros = """{"type":"PURCHASE"}""", antes = loteComSaldo("20"))
                .paraAcao()
                .mudanca,
        )
    }
}
