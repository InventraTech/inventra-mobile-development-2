package com.inventraoficial.inventra.feature.cozinha.create.ui

import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.FakeKitchenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateCozinhaViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var kitchenRepository: FakeKitchenRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var viewModel: CreateCozinhaViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        kitchenRepository = FakeKitchenRepository()
        authRepository = FakeAuthRepository(hasSession = true)
        viewModel = CreateCozinhaViewModel(kitchenRepository, authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sem nome mostra erro e nao chama a API`() {
        viewModel.onNameChange("   ")

        viewModel.onCreateClick()

        assertEquals("Informe o nome da cozinha", viewModel.uiState.value.errorMessage)
        assertEquals(0, kitchenRepository.createCalls)
    }

    @Test
    fun `nome com mais de 120 caracteres mostra erro`() {
        viewModel.onNameChange("a".repeat(121))

        viewModel.onCreateClick()

        assertEquals("O nome pode ter no máximo 120 caracteres", viewModel.uiState.value.errorMessage)
        assertEquals(0, kitchenRepository.createCalls)
    }

    @Test
    fun `liga o loading enquanto cria`() =
        runTest(testDispatcher) {
            viewModel.onNameChange("Sabor Filial Sul")

            viewModel.onCreateClick()

            assertTrue(viewModel.uiState.value.isLoading)
        }

    @Test
    fun `criacao com sucesso atualiza o usuario e marca isCreated`() =
        runTest(testDispatcher) {
            viewModel.onNameChange("  Sabor Filial Sul  ")
            viewModel.onAddressChange("  Av. das Indústrias, 450  ")

            viewModel.onCreateClick()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertTrue(state.isCreated)
            assertFalse(state.isLoading)
            assertEquals("Sabor Filial Sul", kitchenRepository.lastName)
            assertEquals("Av. das Indústrias, 450", kitchenRepository.lastAddress)
            assertEquals(1, authRepository.refreshCalls)
        }

    @Test
    fun `endereco em branco e enviado como nulo`() =
        runTest(testDispatcher) {
            viewModel.onNameChange("Sabor Filial Sul")
            viewModel.onAddressChange("   ")

            viewModel.onCreateClick()
            advanceUntilIdle()

            assertNull(kitchenRepository.lastAddress)
        }

    @Test
    fun `falha na criacao mostra a mensagem e nao navega`() =
        runTest(testDispatcher) {
            kitchenRepository.result = Result.failure(Exception("Você já está vinculado a uma cozinha."))
            viewModel.onNameChange("Sabor Filial Sul")

            viewModel.onCreateClick()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Você já está vinculado a uma cozinha.", state.errorMessage)
            assertFalse(state.isCreated)
            assertFalse(state.isLoading)
            assertEquals(0, authRepository.refreshCalls)
        }

    @Test
    fun `fechar sai da conta e marca isLoggedOut`() =
        runTest(testDispatcher) {
            viewModel.onCloseClick()
            advanceUntilIdle()

            assertEquals(1, authRepository.logoutCalls)
            assertTrue(viewModel.uiState.value.isLoggedOut)
        }
}
