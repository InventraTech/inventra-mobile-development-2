package com.inventraoficial.inventra.feature.assistant.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Guarda a conversa do chat fora do ViewModel.
 *
 * O ViewModel morre quando o usuário sai da tela do chat; o estado e as chamadas em andamento
 * ficam aqui (um por app) e a conversa continua de onde parou quando ele volta, até ser encerrada.
 */
class ChatSessionStore(
    /** Escopo que sobrevive à tela: uma resposta que chega depois de o usuário sair não se perde. */
    val escopo: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    val estado = MutableStateFlow(ChatUiState(sessionId = novaSessaoId()))

    /** A resposta que está chegando (stream); "Nova conversa" a cancela para ela não cair na conversa nova. */
    var envioEmAndamento: Job? = null

    /** Conversas que o usuário deixou mas que o servidor ainda não conseguiu encerrar; tenta de novo depois. */
    val sessoesParaEncerrar: MutableList<String> = mutableListOf()

    /** Id do usuário dono da conversa guardada; se outro usuário entrar, a conversa é descartada. */
    var donoId: String? = null

    fun reiniciar() {
        estado.value = ChatUiState(sessionId = novaSessaoId())
        donoId = null
    }
}
