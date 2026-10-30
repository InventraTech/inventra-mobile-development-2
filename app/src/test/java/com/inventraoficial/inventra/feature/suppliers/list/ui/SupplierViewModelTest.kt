package com.inventraoficial.inventra.feature.suppliers.list.ui

import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import com.inventraoficial.inventra.data.repository.FakeSupplierRepository
import com.inventraoficial.inventra.data.repository.FakeSupplierRepository.Companion.supplier
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
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SupplierViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val user =
        SessionUser(
            id = "sup-1",
            name = "Jones",
            email = "jones@inventra.com",
            accessType = AccessType.SUPERVISOR,
            kitchenId = 7,
            kitchenName = "Sabor Filial Sul",
        )
    private lateinit var supplierRepository: FakeSupplierRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        supplierRepository =
            FakeSupplierRepository(
                suppliers =
                    listOf(
                        supplier(3, "camil alimentos", email = "vendas@camil.com.br"),
                        supplier(2, "Friboi", rating = 4, whatsapp = "11999990000"),
                        supplier(1, "Seara Alimentos", rating = 5, email = "contato@seara.com.br", whatsapp = " "),
                    ),
            )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(sessionUser: SessionUser? = user) =
        SupplierViewModel(supplierRepository, FakeAuthRepository(initialUser = sessionUser))

    private fun SupplierViewModel.names() = uiState.value.suppliers.map { it.name }

    private fun SupplierViewModel.applyFilters(change: SupplierViewModel.() -> Unit) {
        onFilterIconClick()
        change()
        onApplyFilters()
    }

    @Test
    fun `busca os fornecedores da cozinha do usuario em ordem alfabetica`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals(listOf("camil alimentos", "Friboi", "Seara Alimentos"), viewModel.names())
            assertEquals(listOf(7), supplierRepository.kitchenCalls)
        }

    @Test
    fun `usa o cnpj no selo e zero estrelas quando nao tem nota`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            val camil =
                viewModel.uiState.value.suppliers
                    .first()
            assertEquals("00.000.000/0001-03", camil.badgeText)
            assertEquals(0, camil.rating)
            assertEquals(3, camil.id)
        }

    @Test
    fun `busca filtra por nome`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onSearchQueryChange("SEARA ")

            assertEquals(listOf("Seara Alimentos"), viewModel.names())
        }

    @Test
    fun `busca sem correspondencia retorna lista vazia`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onSearchQueryChange("fornecedor que nao existe")

            assertTrue(viewModel.names().isEmpty())
        }

    @Test
    fun `usuario sem cozinha nao chama a api`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(user.copy(kitchenId = null, kitchenName = null))
            advanceUntilIdle()

            assertTrue(viewModel.names().isEmpty())
            assertTrue(supplierRepository.kitchenCalls.isEmpty())
        }

    @Test
    fun `falha na api mostra o erro e tentar novamente recarrega`() =
        runTest(testDispatcher) {
            supplierRepository.error = IOException("Sem conexão")
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertEquals("Sem conexão", viewModel.uiState.value.errorMessage)

            supplierRepository.error = null
            viewModel.onRetryClick()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.errorMessage)
            assertEquals(3, viewModel.names().size)
        }

    @Test
    fun `ordena pela melhor avaliacao com os sem nota no fim`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.applyFilters { onSortSelect(SupplierSort.Rating) }

            assertEquals(listOf("Seara Alimentos", "Friboi", "camil alimentos"), viewModel.names())
            assertEquals(1, viewModel.uiState.value.filters.activeCount)
        }

    @Test
    fun `avaliacao minima esconde quem tem menos estrelas ou nenhuma`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.applyFilters { onMinRatingSelect(MinRating.Four) }
            assertEquals(listOf("Friboi", "Seara Alimentos"), viewModel.names())

            viewModel.applyFilters { onMinRatingSelect(MinRating.Five) }
            assertEquals(listOf("Seara Alimentos"), viewModel.names())
        }

    @Test
    fun `contato exige todos os meios marcados e ignora valores em branco`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.applyFilters { onContactToggle(SupplierContact.WhatsApp) }
            assertEquals(listOf("Friboi"), viewModel.names())

            viewModel.applyFilters {
                onContactToggle(SupplierContact.WhatsApp)
                onContactToggle(SupplierContact.Email)
            }
            assertEquals(listOf("camil alimentos", "Seara Alimentos"), viewModel.names())
        }

    @Test
    fun `filtros combinam com a busca`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.applyFilters { onContactToggle(SupplierContact.Email) }
            viewModel.onSearchQueryChange("seara")

            assertEquals(listOf("Seara Alimentos"), viewModel.names())
        }

    @Test
    fun `rascunho so vale depois de aplicar e fechar descarta`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onFilterIconClick()
            viewModel.onMinRatingSelect(MinRating.Five)
            assertEquals(3, viewModel.names().size)

            viewModel.onFiltersDismiss()

            assertNull(viewModel.uiState.value.draftFilters)
            assertEquals(SupplierFilters(), viewModel.uiState.value.filters)
            assertEquals(3, viewModel.names().size)
        }

    @Test
    fun `limpar volta tudo ao padrao`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.applyFilters {
                onSortSelect(SupplierSort.Rating)
                onMinRatingSelect(MinRating.Three)
                onContactToggle(SupplierContact.Email)
            }
            assertEquals(3, viewModel.uiState.value.filters.activeCount)

            viewModel.applyFilters { onClearFilters() }

            assertEquals(SupplierFilters(), viewModel.uiState.value.filters)
            assertEquals(3, viewModel.names().size)
        }
}
