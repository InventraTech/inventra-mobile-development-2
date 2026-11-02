package com.inventraoficial.inventra.feature.assistant.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatSessionStoreTest {
    @Test
    fun `reiniciar esquece a conversa, o dono e o que estava pendente`() {
        val store = ChatSessionStore(escopo = CoroutineScope(Job()))
        val respostaEmAndamento = Job()
        val sessaoAntiga = store.estado.value.sessionId
        store.estado.value = store.estado.value.copy(mensagens = listOf(Mensagem("1", "oi", ehDoUsuario = true)))
        store.donoId = "id-supervisor"
        store.envioEmAndamento = respostaEmAndamento
        store.sessoesParaEncerrar += "sessao-do-supervisor"

        store.reiniciar()

        assertTrue(respostaEmAndamento.isCancelled)
        assertNull(store.envioEmAndamento)
        assertNull(store.donoId)
        assertTrue(store.sessoesParaEncerrar.isEmpty())
        assertTrue(
            store.estado.value.mensagens
                .isEmpty(),
        )
        assertNotEquals(sessaoAntiga, store.estado.value.sessionId)
        assertEquals("", store.estado.value.inputValue)
    }
}
