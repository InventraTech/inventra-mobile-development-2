package com.inventraoficial.inventra.feature.suppliers.create.ui

import com.inventraoficial.inventra.data.remote.dto.supplier.CreateSupplierRequest
import com.inventraoficial.inventra.data.repository.FakeSupplierRepository
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
class SupplierCreateViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeSupplierRepository
    private lateinit var viewModel: SupplierCreateViewModel
    private val atacadao = FakeSupplierRepository.supplier(5, "Atacadão").copy(cnpj = "12.345.678/0001-90")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeSupplierRepository()
        viewModel = SupplierCreateViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun fillRequired() {
        viewModel.onLegalNameChange("  Atacadão Distribuidora ")
        viewModel.onCnpjChange("12345678000190")
    }

    @Test
    fun `cadastra o fornecedor com o CNPJ formatado e mostra a confirmacao`() =
        runTest(testDispatcher) {
            fillRequired()
            viewModel.onEmailChange(" vendas@atacadao.com ")
            viewModel.onWhatsappChange("11999990000")
            viewModel.onRatingSelect(4)

            viewModel.onSaveClick()
            assertTrue(viewModel.uiState.value.isSubmitting)
            advanceUntilIdle()

            assertEquals(
                listOf(
                    CreateSupplierRequest(
                        legalName = "Atacadão Distribuidora",
                        cnpj = "12.345.678/0001-90",
                        email = "vendas@atacadao.com",
                        whatsapp = "11999990000",
                        rating = 4,
                    ),
                ),
                repository.created,
            )
            assertEquals(
                CreatedSupplier(FakeSupplierRepository.NEW_ID, "Atacadão Distribuidora"),
                viewModel.uiState.value.created,
            )
            assertFalse(viewModel.uiState.value.isSubmitting)
        }

    @Test
    fun `campos opcionais vazios vao como nulos`() =
        runTest(testDispatcher) {
            fillRequired()

            viewModel.onSaveClick()
            advanceUntilIdle()

            val request = repository.created.single()
            assertNull(request.email)
            assertNull(request.whatsapp)
            assertNull(request.rating)
        }

    @Test
    fun `salvar exige razao social e CNPJ completo`() =
        runTest(testDispatcher) {
            viewModel.onSaveClick()
            assertEquals("Informe a razão social do fornecedor", viewModel.uiState.value.submitError)

            viewModel.onLegalNameChange("Atacadão")
            viewModel.onCnpjChange("1234567800019")
            viewModel.onSaveClick()
            assertEquals("O CNPJ precisa ter 14 números", viewModel.uiState.value.submitError)
            assertTrue(repository.created.isEmpty())
        }

    @Test
    fun `e-mail invalido nao deixa salvar`() =
        runTest(testDispatcher) {
            fillRequired()
            viewModel.onEmailChange("vendas.atacadao.com")

            viewModel.onSaveClick()

            assertEquals("Informe um e-mail válido", viewModel.uiState.value.submitError)
            assertTrue(repository.created.isEmpty())
        }

    @Test
    fun `CNPJ e WhatsApp so aceitam numeros e o CNPJ para em 14`() {
        viewModel.onCnpjChange("12.345")
        viewModel.onWhatsappChange("(11) 9")
        assertEquals("", viewModel.uiState.value.cnpj)
        assertEquals("", viewModel.uiState.value.whatsapp)

        viewModel.onCnpjChange("123456780001901")
        viewModel.onCnpjChange("12345678000190")
        assertEquals("12345678000190", viewModel.uiState.value.cnpj)
    }

    @Test
    fun `tocar na mesma estrela tira a avaliacao`() {
        viewModel.onRatingSelect(3)
        assertEquals(3, viewModel.uiState.value.rating)

        viewModel.onRatingSelect(3)
        assertEquals(0, viewModel.uiState.value.rating)
    }

    @Test
    fun `CNPJ repetido mostra a mensagem do back`() =
        runTest(testDispatcher) {
            repository.createResult = Result.failure(Exception("Já existe um fornecedor com esse CNPJ."))
            fillRequired()

            viewModel.onSaveClick()
            advanceUntilIdle()

            assertEquals("Já existe um fornecedor com esse CNPJ.", viewModel.uiState.value.submitError)
            assertNull(viewModel.uiState.value.created)
        }

    @Test
    fun `CNPJ que ja existe oferece usar o fornecedor cadastrado`() =
        runTest(testDispatcher) {
            repository.suppliers = listOf(atacadao)
            repository.createResult = Result.failure(Exception("Já existe um fornecedor com esse CNPJ."))
            fillRequired()

            viewModel.onSaveClick()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.submitError)
            assertEquals(CreatedSupplier(5, "Atacadão"), viewModel.uiState.value.existing)

            viewModel.onUseExistingClick()

            assertEquals(CreatedSupplier(5, "Atacadão"), viewModel.uiState.value.created)
        }

    @Test
    fun `mudar o CNPJ tira a sugestao do fornecedor existente`() =
        runTest(testDispatcher) {
            repository.suppliers = listOf(atacadao)
            repository.createResult = Result.failure(Exception("Já existe um fornecedor com esse CNPJ."))
            fillRequired()
            viewModel.onSaveClick()
            advanceUntilIdle()

            viewModel.onCnpjChange("1234567800019")

            assertNull(viewModel.uiState.value.existing)
        }
}
