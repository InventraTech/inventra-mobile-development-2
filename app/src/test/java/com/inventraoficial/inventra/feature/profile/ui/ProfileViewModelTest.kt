package com.inventraoficial.inventra.feature.profile.ui

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `mostra nome, cargo e cozinha do usuario logado`() =
        runTest(testDispatcher) {
            val viewModel = ProfileViewModel(FakeAuthRepository(initialUser = estoquista))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Ana Souza", state.fullName)
            assertEquals("Estoquista", state.roleLabel)
            assertEquals("Cozinha Central", state.cozinhaName)
        }

    @Test
    fun `usuario sem cozinha mostra a mensagem no lugar do nome da cozinha`() =
        runTest(testDispatcher) {
            val semCozinha = estoquista.copy(kitchenId = null, kitchenName = null)
            val viewModel = ProfileViewModel(FakeAuthRepository(initialUser = semCozinha))
            advanceUntilIdle()

            assertEquals("Nenhuma cozinha vinculada", viewModel.uiState.value.cozinhaName)
        }

    @Test
    fun `sem usuario os campos ficam vazios`() =
        runTest(testDispatcher) {
            val viewModel = ProfileViewModel(FakeAuthRepository(initialUser = null))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("", state.fullName)
            assertEquals("", state.roleLabel)
        }

    private companion object {
        val estoquista =
            SessionUser(
                id = "00000000-0000-0000-0000-000000000002",
                name = "Ana Souza",
                email = "ana@inventra.com",
                accessType = AccessType.ESTOQUISTA,
                kitchenId = 3,
                kitchenName = "Cozinha Central",
            )
    }
}
