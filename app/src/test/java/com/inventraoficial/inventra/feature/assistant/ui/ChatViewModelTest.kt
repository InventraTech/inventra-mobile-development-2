package com.inventraoficial.inventra.feature.assistant.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatViewModelTest {
    @Test
    fun `estado inicial tem saudacao, sugestoes e input vazio`() {
        val viewModel = ChatViewModel()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.greeting.isNotBlank())
        assertEquals(3, uiState.suggestions.size)
        assertEquals("", uiState.inputValue)
    }

    @Test
    fun `onInputChange atualiza o campo de texto`() {
        val viewModel = ChatViewModel()

        viewModel.onInputChange("Qual o estoque de leite?")

        assertEquals("Qual o estoque de leite?", viewModel.uiState.value.inputValue)
    }

    @Test
    fun `onSuggestionClick preenche o campo com a sugestao`() {
        val viewModel = ChatViewModel()
        val suggestion = viewModel.uiState.value.suggestions.first()

        viewModel.onSuggestionClick(suggestion)

        assertEquals(suggestion, viewModel.uiState.value.inputValue)
    }

    @Test
    fun `onSend limpa o campo de texto`() {
        val viewModel = ChatViewModel()
        viewModel.onInputChange("pergunta qualquer")

        viewModel.onSend()

        assertEquals("", viewModel.uiState.value.inputValue)
    }
}
