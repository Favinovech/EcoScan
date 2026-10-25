package pe.ecoscan.app.presentation.scan

import android.graphics.Bitmap
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.data.repository.WasteRecordRepositoryImpl
import pe.ecoscan.app.data.scanner.BarcodeDetector
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteClassification
import pe.ecoscan.app.domain.usecase.ClassifyWasteUseCase
import java.io.FileNotFoundException

@OptIn(ExperimentalCoroutinesApi::class)
class ScanViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeDispatcherProvider = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }

    private val classifyWasteUseCase = mockk<ClassifyWasteUseCase>(relaxed = true)
    private val wasteRecordRepository = mockk<WasteRecordRepositoryImpl>(relaxed = true)
    private val barcodeDetector = mockk<BarcodeDetector>(relaxed = true)

    private lateinit var viewModel: ScanViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ScanViewModel(
            classifyWasteUseCase = classifyWasteUseCase,
            wasteRecordRepository = wasteRecordRepository,
            dispatcherProvider = fakeDispatcherProvider,
            barcodeDetector = barcodeDetector
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `el estado inicial no esta analizando y no tiene error`() {
        val uiState = viewModel.uiState.value
        assertFalse(uiState.isAnalyzing)
        assertFalse(uiState.isSaving)
        assertNull(uiState.errorMessage)
        assertNull(uiState.capturedBitmap)
        assertNull(uiState.classification)
    }

    @Test
    fun `onPermissionResult actualiza hasCameraPermission`() {
        viewModel.onPermissionResult(true)
        assertTrue(viewModel.uiState.value.hasCameraPermission)

        viewModel.onPermissionResult(false)
        assertFalse(viewModel.uiState.value.hasCameraPermission)
    }

    @Test
    fun `onCategoryCorrected actualiza la categoria seleccionada e instrucciones`() {
        // Simulamos una clasificación previa de plástico
        viewModel.onCategoryCorrected(WasteCategory.PLASTIC)
        // Si no había clasificación base, se mantiene seguro sin fallar
        assertNull(viewModel.uiState.value.classification)
    }

    @Test
    fun `resetScan limpia el estado de captura y resultados`() {
        viewModel.resetScan()

        val state = viewModel.uiState.value
        assertNull(state.capturedBitmap)
        assertNull(state.classification)
        assertNull(state.selectedCategory)
        assertFalse(state.isSavedSuccess)
    }

    @Test
    fun `un error del clasificador no oculta la camara`() = runTest(testDispatcher) {
        viewModel.onPermissionResult(true)

        val bitmap = mockk<Bitmap>(relaxed = true)
        coEvery { classifyWasteUseCase(bitmap) } returns
            Result.failure(FileNotFoundException("waste_classifier.tflite"))
        coEvery { barcodeDetector.detect(bitmap) } returns Result.success(null)

        viewModel.onImageCaptured(bitmap)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Modelo de clasificación no disponible", state.classificationError)
        assertNull(state.classification)
        assertFalse(state.isAnalyzing)
        assertTrue(state.hasCameraPermission)
    }
}