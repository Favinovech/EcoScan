package pe.ecoscan.app.presentation.product

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
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
import pe.ecoscan.app.core.common.ErrorType
import pe.ecoscan.app.core.common.Resource
import pe.ecoscan.app.domain.model.PackagedProduct
import pe.ecoscan.app.domain.usecase.GetProductByBarcodeUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeDispatcherProvider = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }

    private val getProductByBarcodeUseCase = mockk<GetProductByBarcodeUseCase>()

    private lateinit var viewModel: ProductViewModel

    private val barcode = "7751271014013"

    private val product = PackagedProduct(
        barcode = barcode,
        name = "Agua Mineral",
        brand = "San Luis",
        packagingMaterials = listOf("Plástico"),
        imageUrl = null,
        disposalHint = "Enjuaga el envase y sepáralo como plástico."
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ProductViewModel(
            getProductByBarcodeUseCase = getProductByBarcodeUseCase,
            dispatcherProvider = fakeDispatcherProvider
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `consultar pasa por carga y luego exito con los datos del producto`() = runTest(testDispatcher) {
        coEvery { getProductByBarcodeUseCase(barcode) } coAnswers {
            kotlinx.coroutines.delay(100)
            Resource.Success(product)
        }

        viewModel.uiState.test {
            assertFalse(awaitItem().isLoading)

            viewModel.consultar(barcode)

            assertTrue(awaitItem().isLoading)

            testDispatcher.scheduler.advanceUntilIdle()

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertEquals(product, successState.product)
            assertNull(successState.error)
            assertFalse(successState.isFromCache)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `consultar con exito desde cache marca isFromCache`() = runTest(testDispatcher) {
        coEvery { getProductByBarcodeUseCase(barcode) } returns Resource.Success(product, isStale = true)

        viewModel.consultar(barcode)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isFromCache)
        assertEquals(product, state.product)
    }

    @Test
    fun `consultar con producto no encontrado refleja ErrorType NOT_FOUND`() = runTest(testDispatcher) {
        coEvery { getProductByBarcodeUseCase(barcode) } returns Resource.Error(
            "No se encontró el producto",
            ErrorType.NOT_FOUND
        )

        viewModel.consultar(barcode)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(ErrorType.NOT_FOUND, state.error)
        assertNull(state.product)
        assertFalse(state.isLoading)
    }

    @Test
    fun `consultar sin conexion refleja ErrorType NO_CONNECTION`() = runTest(testDispatcher) {
        coEvery { getProductByBarcodeUseCase(barcode) } returns Resource.Error(
            "Sin conexión a internet.",
            ErrorType.NO_CONNECTION
        )

        viewModel.consultar(barcode)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ErrorType.NO_CONNECTION, viewModel.uiState.value.error)
    }

    @Test
    fun `consultar con timeout refleja ErrorType TIMEOUT`() = runTest(testDispatcher) {
        coEvery { getProductByBarcodeUseCase(barcode) } returns Resource.Error(
            "La consulta tardó demasiado.",
            ErrorType.TIMEOUT
        )

        viewModel.consultar(barcode)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ErrorType.TIMEOUT, viewModel.uiState.value.error)
    }

    @Test
    fun `consultar con error de servidor refleja ErrorType SERVER_ERROR`() = runTest(testDispatcher) {
        coEvery { getProductByBarcodeUseCase(barcode) } returns Resource.Error(
            "El servidor no respondió correctamente.",
            ErrorType.SERVER_ERROR
        )

        viewModel.consultar(barcode)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ErrorType.SERVER_ERROR, viewModel.uiState.value.error)
    }

    @Test
    fun `reintentar vuelve a invocar el caso de uso con el mismo codigo`() = runTest(testDispatcher) {
        coEvery { getProductByBarcodeUseCase(barcode) } returns Resource.Error(
            "Sin conexión a internet.",
            ErrorType.NO_CONNECTION
        )

        viewModel.consultar(barcode)
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery { getProductByBarcodeUseCase(barcode) } returns Resource.Success(product)

        viewModel.reintentar()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 2) { getProductByBarcodeUseCase(barcode) }
        assertEquals(product, viewModel.uiState.value.product)
        assertNull(viewModel.uiState.value.error)
    }
}
