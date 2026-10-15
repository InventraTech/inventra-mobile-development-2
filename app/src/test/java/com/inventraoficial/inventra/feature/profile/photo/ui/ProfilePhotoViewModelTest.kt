package com.inventraoficial.inventra.feature.profile.photo.ui

import com.inventraoficial.inventra.data.local.ImageCompressor
import com.inventraoficial.inventra.data.local.ProfilePhotoPreview
import com.inventraoficial.inventra.data.local.SessionUser
import com.inventraoficial.inventra.data.remote.dto.auth.AccessType
import com.inventraoficial.inventra.data.repository.FakeAuthRepository
import kotlinx.coroutines.CompletableDeferred
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
class ProfilePhotoViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val escolhida = "content://media/picker/0/1"
    private val compressed = mutableListOf<String>()
    private var compressResult: Result<ByteArray> = Result.success(byteArrayOf(9, 9))
    private var compressGate: CompletableDeferred<Unit>? = null
    private val compressor =
        ImageCompressor { uri ->
            compressed += uri
            compressGate?.await()
            compressResult
        }
    private lateinit var preview: ProfilePhotoPreview
    private lateinit var repository: FakeAuthRepository
    private lateinit var viewModel: ProfilePhotoViewModel

    private val ana =
        SessionUser(
            id = "1",
            name = "Ana Souza",
            email = "ana@inventra.com",
            accessType = AccessType.ESTOQUISTA,
            kitchenId = 7,
            kitchenName = "Cozinha Central",
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        preview = ProfilePhotoPreview()
        criarViewModel(ana)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun criarViewModel(user: SessionUser?) {
        repository = FakeAuthRepository(initialUser = user)
        viewModel = ProfilePhotoViewModel(repository, compressor, preview)
    }

    @Test
    fun `mostra as iniciais e a foto salva do usuario`() =
        runTest(testDispatcher) {
            criarViewModel(ana.copy(photoUrl = FakeAuthRepository.UPLOADED_PHOTO_URL))
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals("AS", state.initials)
            assertEquals(FakeAuthRepository.UPLOADED_PHOTO_URL, state.photoUrl)
        }

    @Test
    fun `tocar no avatar abre as opcoes de foto`() =
        runTest(testDispatcher) {
            viewModel.onAvatarClick()

            assertTrue(viewModel.uiState.value.isSheetVisible)
        }

    @Test
    fun `escolher uma foto mostra a imagem na hora, antes do envio terminar`() =
        runTest(testDispatcher) {
            advanceUntilIdle()
            compressGate = CompletableDeferred()

            viewModel.onPhotoPicked(escolhida)
            advanceUntilIdle()

            assertEquals(escolhida, viewModel.uiState.value.photoUrl)
            assertTrue(viewModel.uiState.value.isUploading)

            compressGate?.complete(Unit)
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.isUploading)
        }

    @Test
    fun `envio com sucesso troca a previa pela foto do servidor`() =
        runTest(testDispatcher) {
            viewModel.onPhotoPicked(escolhida)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(listOf(escolhida), compressed)
            assertEquals(listOf(9, 9), repository.uploadedPhotos.single().map { it.toInt() })
            assertEquals(FakeAuthRepository.UPLOADED_PHOTO_URL, state.photoUrl)
            assertNull(preview.uri.value)
            assertFalse(state.isUploading)
        }

    @Test
    fun `envio que falha mantem a previa e mostra o erro`() =
        runTest(testDispatcher) {
            repository.photoResult = Result.failure(Exception("Erro inesperado. Tente novamente."))

            viewModel.onPhotoPicked(escolhida)
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(escolhida, state.photoUrl)
            assertEquals("Erro inesperado. Tente novamente.", state.errorMessage)
            assertFalse(state.isUploading)
        }

    @Test
    fun `a previa aparece tambem na outra tela de perfil`() =
        runTest(testDispatcher) {
            repository.photoResult = Result.failure(Exception("sem endpoint"))
            viewModel.onPhotoPicked(escolhida)
            advanceUntilIdle()

            val outraTela = ProfilePhotoViewModel(FakeAuthRepository(initialUser = ana), compressor, preview)
            advanceUntilIdle()

            assertEquals(escolhida, outraTela.uiState.value.photoUrl)
        }

    @Test
    fun `fechar o seletor sem escolher nao envia nada`() =
        runTest(testDispatcher) {
            viewModel.onPhotoPicked(null)
            advanceUntilIdle()

            assertTrue(repository.uploadedPhotos.isEmpty())
            assertNull(viewModel.uiState.value.photoUrl)
            assertFalse(viewModel.uiState.value.isUploading)
        }

    @Test
    fun `imagem que nao pode ser lida mostra o erro sem chamar a api`() =
        runTest(testDispatcher) {
            compressResult = Result.failure(Exception("Não foi possível ler a imagem escolhida."))

            viewModel.onPhotoPicked("content://quebrada")
            advanceUntilIdle()

            assertEquals("Não foi possível ler a imagem escolhida.", viewModel.uiState.value.errorMessage)
            assertTrue(repository.uploadedPhotos.isEmpty())
        }

    @Test
    fun `remover a foto limpa a previa e a foto salva`() =
        runTest(testDispatcher) {
            criarViewModel(ana.copy(photoUrl = FakeAuthRepository.UPLOADED_PHOTO_URL))
            preview.show(escolhida)
            advanceUntilIdle()

            viewModel.onRemoveClick()
            advanceUntilIdle()

            assertEquals(1, repository.removePhotoCalls)
            assertFalse(viewModel.uiState.value.hasPhoto)
        }
}
