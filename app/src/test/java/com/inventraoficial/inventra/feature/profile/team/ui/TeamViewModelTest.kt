package com.inventraoficial.inventra.feature.profile.team.ui

import com.inventraoficial.inventra.data.local.toSessionUser
import com.inventraoficial.inventra.data.remote.dto.kitchenaccess.AccessRequestUser
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
class TeamViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var membersRepository: FakeMembersRepository
    private val paula =
        FakeMembersRepository.estoquista(
            AccessRequestUser(id = "est-2", name = "Paula Lima", email = "paula@inventra.com"),
        )
    private val ana =
        FakeMembersRepository.estoquista(
            AccessRequestUser(id = "est-3", name = "Ana Souza", email = "ana@inventra.com"),
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        membersRepository = FakeMembersRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TeamViewModel {
        val currentUser = FakeMembersRepository.supervisor.toSessionUser()
        return TeamViewModel(membersRepository, FakeAuthRepository(initialUser = currentUser))
    }

    @Test
    fun `o usuario logado aparece primeiro como voce e os outros em ordem de nome`() =
        runTest(testDispatcher) {
            membersRepository.members.addAll(listOf(paula, ana))
            val viewModel = createViewModel()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(listOf("Jones Silva (você)", "Ana Souza", "Paula Lima"), state.members.map { it.name })
            assertTrue(state.members.first().isCurrentUser)
            assertEquals("Supervisor", state.members.first().roleLabel)
            assertEquals("PL", state.members.last().initials)
        }

    @Test
    fun `membros inativos nao aparecem na equipe`() =
        runTest(testDispatcher) {
            membersRepository.members.add(paula.copy(active = false))
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertEquals(
                listOf("Jones Silva (você)"),
                viewModel.uiState.value.members
                    .map { it.name },
            )
        }

    @Test
    fun `nao busca os pedidos de entrada`() =
        runTest(testDispatcher) {
            membersRepository.pendingError = Exception("Não deveria ser chamado")
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertEquals(1, viewModel.uiState.value.members.size)
        }

    @Test
    fun `erro ao carregar mostra a mensagem e tentar de novo recarrega`() =
        runTest(testDispatcher) {
            membersRepository.membersError = Exception("Não foi possível conectar ao servidor. Verifique sua conexão.")
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertEquals(
                "Não foi possível conectar ao servidor. Verifique sua conexão.",
                viewModel.uiState.value.errorMessage,
            )

            membersRepository.membersError = null
            viewModel.onRetryClick()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertEquals(1, viewModel.uiState.value.members.size)
        }

    @Test
    fun `iniciais usam a primeira e a ultima palavra do nome`() {
        assertEquals("JP", "Jones Arakaki Kogake Passos".initials())
        assertEquals("M", "Mauro".initials())
        assertEquals("", "   ".initials())
    }
}
