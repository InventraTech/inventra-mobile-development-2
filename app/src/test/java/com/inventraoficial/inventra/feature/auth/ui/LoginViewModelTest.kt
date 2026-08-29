package com.inventraoficial.inventra.feature.auth.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoginViewModelTest {
    @Test
    fun `onLoginChange atualiza o login no estado`() {
        val viewModel = LoginViewModel()

        viewModel.onLoginChange("felipe")

        assertEquals("felipe", viewModel.uiState.value.login)
    }

    @Test
    fun `onPasswordChange atualiza a senha no estado`() {
        val viewModel = LoginViewModel()

        viewModel.onPasswordChange("123456")

        assertEquals("123456", viewModel.uiState.value.password)
    }

    @Test
    fun `onLoginClick com campos vazios preenche errorMessage`() {
        val viewModel = LoginViewModel()

        viewModel.onLoginClick()

        assertEquals("Preencha todos os campos", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onLoginClick com login vazio e senha preenchida ainda preenche errorMessage`() {
        val viewModel = LoginViewModel()
        viewModel.onPasswordChange("123456")

        viewModel.onLoginClick()

        assertEquals("Preencha todos os campos", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `onLoginClick com os dois campos preenchidos nao preenche errorMessage`() {
        val viewModel = LoginViewModel()
        viewModel.onLoginChange("felipe")
        viewModel.onPasswordChange("123456")

        viewModel.onLoginClick()

        assertNull(viewModel.uiState.value.errorMessage)
    }
}
