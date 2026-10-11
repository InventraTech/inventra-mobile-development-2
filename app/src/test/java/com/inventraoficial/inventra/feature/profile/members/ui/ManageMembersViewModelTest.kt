package com.inventraoficial.inventra.feature.profile.members.ui

import com.inventraoficial.inventra.data.local.toSessionUser
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.FakeMembersRepository
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
class ManageMembersViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var membersRepository: FakeMembersRepository
    private lateinit var viewModel: ManageMembersViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        membersRepository = FakeMembersRepository()
        val currentUser = FakeMembersRepository.supervisor.toSessionUser()
        viewModel = ManageMembersViewModel(membersRepository, FakeAuthRepository(initialUser = currentUser))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `carrega as solicitacoes pendentes e os membros`() =
        runTest(testDispatcher) {
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(listOf("Mauro Rosa"), state.pendingRequests.map { it.name })
            assertEquals("mauro@inventra.com", state.pendingRequests.first().email)
            assertEquals("MR", state.pendingRequests.first().initials)
        }

    @Test
    fun `o usuario logado aparece como voce`() =
        runTest(testDispatcher) {
            advanceUntilIdle()

            val me =
                viewModel.uiState.value.members
                    .single()
            assertEquals("Jones Silva (você)", me.name)
            assertEquals("Supervisor", me.roleLabel)
            assertTrue(me.isCurrentUser)
        }

    @Test
    fun `aprovar tira da lista de pendentes e coloca nos membros`() =
        runTest(testDispatcher) {
            advanceUntilIdle()

            viewModel.onAcceptClick(
                viewModel.uiState.value.pendingRequests
                    .first(),
            )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(listOf("req-1"), membersRepository.approvedIds)
            assertTrue(state.pendingRequests.isEmpty())
            assertEquals(listOf("Jones Silva (você)", "Mauro Rosa"), state.members.map { it.name })
            assertNull(state.processingRequestId)
        }

    @Test
    fun `falha ao aprovar mostra a mensagem e mantem a solicitacao`() =
        runTest(testDispatcher) {
            advanceUntilIdle()
            membersRepository.approveError = Exception("Essa solicitação já foi decidida ou expirou.")

            viewModel.onAcceptClick(
                viewModel.uiState.value.pendingRequests
                    .first(),
            )
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("Essa solicitação já foi decidida ou expirou.", state.actionErrorMessage)
            assertEquals(1, state.pendingRequests.size)
        }

    @Test
    fun `recusar abre o dialogo e so recusa ao confirmar, com o motivo`() =
        runTest(testDispatcher) {
            advanceUntilIdle()
            val request =
                viewModel.uiState.value.pendingRequests
                    .first()

            viewModel.onRejectClick(request)
            assertEquals(request, viewModel.uiState.value.rejectTarget)
            assertTrue(membersRepository.rejected.isEmpty())

            viewModel.onRejectReasonChange("  Não faz parte da equipe  ")
            viewModel.onRejectConfirm()
            advanceUntilIdle()

            assertEquals(listOf("req-1" to "Não faz parte da equipe"), membersRepository.rejected)
            assertNull(viewModel.uiState.value.rejectTarget)
            assertTrue(
                viewModel.uiState.value.pendingRequests
                    .isEmpty(),
            )
        }

    @Test
    fun `motivo em branco e enviado como nulo`() =
        runTest(testDispatcher) {
            advanceUntilIdle()

            viewModel.onRejectClick(
                viewModel.uiState.value.pendingRequests
                    .first(),
            )
            viewModel.onRejectReasonChange("   ")
            viewModel.onRejectConfirm()
            advanceUntilIdle()

            assertEquals(listOf("req-1" to null), membersRepository.rejected)
        }

    @Test
    fun `cancelar o dialogo nao recusa`() =
        runTest(testDispatcher) {
            advanceUntilIdle()

            viewModel.onRejectClick(
                viewModel.uiState.value.pendingRequests
                    .first(),
            )
            viewModel.onRejectDismiss()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.rejectTarget)
            assertTrue(membersRepository.rejected.isEmpty())
            assertEquals(1, viewModel.uiState.value.pendingRequests.size)
        }

    @Test
    fun `erro ao carregar mostra a mensagem e tentar de novo recarrega`() =
        runTest(testDispatcher) {
            membersRepository.pendingError = Exception("Não foi possível conectar ao servidor. Verifique sua conexão.")
            val viewModelComErro = ManageMembersViewModel(membersRepository, FakeAuthRepository())
            advanceUntilIdle()
            assertEquals(
                "Não foi possível conectar ao servidor. Verifique sua conexão.",
                viewModelComErro.uiState.value.errorMessage,
            )

            membersRepository.pendingError = null
            viewModelComErro.onRetryClick()
            advanceUntilIdle()

            assertNull(viewModelComErro.uiState.value.errorMessage)
            assertEquals(1, viewModelComErro.uiState.value.pendingRequests.size)
        }

    @Test
    fun `iniciais usam a primeira e a ultima palavra do nome`() {
        assertEquals("JP", "Jones Arakaki Kogake Passos".initials())
        assertEquals("M", "Mauro".initials())
        assertEquals("", "   ".initials())
    }
}
