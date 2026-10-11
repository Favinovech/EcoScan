package pe.ecoscan.app.presentation.history

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteRecord
import pe.ecoscan.app.domain.usecase.DeleteWasteRecordUseCase
import pe.ecoscan.app.domain.usecase.FilterWasteRecordsUseCase
import pe.ecoscan.app.domain.usecase.GetTotalPointsUseCase
import pe.ecoscan.app.domain.usecase.GetWasteRecordsUseCase
import pe.ecoscan.app.domain.usecase.UpdateWasteRecordUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeDispatcherProvider = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }

    private val plastic = WasteRecord("1", WasteCategory.PLASTIC, weightKg = 2.5, points = 25, recordedAt = 0L)
    private val glass = WasteRecord("2", WasteCategory.GLASS, weightKg = 5.0, points = 30, recordedAt = 0L)
    private val records = listOf(plastic, glass)

    private val getTotalPointsUseCase = mockk<GetTotalPointsUseCase>()
    private val updateWasteRecordUseCase = mockk<UpdateWasteRecordUseCase>()
    private val deleteWasteRecordUseCase = mockk<DeleteWasteRecordUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getTotalPointsUseCase(any()) } returns 55
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(getWasteRecordsUseCase: GetWasteRecordsUseCase): HistoryViewModel =
        HistoryViewModel(
            getWasteRecordsUseCase = getWasteRecordsUseCase,
            getTotalPointsUseCase = getTotalPointsUseCase,
            filterWasteRecordsUseCase = FilterWasteRecordsUseCase(),
            updateWasteRecordUseCase = updateWasteRecordUseCase,
            deleteWasteRecordUseCase = deleteWasteRecordUseCase,
            dispatcherProvider = fakeDispatcherProvider
        )

    private fun createViewModelWith(list: List<WasteRecord>): HistoryViewModel {
        val useCase = mockk<GetWasteRecordsUseCase>()
        every { useCase() } returns flowOf(list)
        return createViewModel(useCase)
    }

    @Test
    fun `uiState transiciona de carga a datos cuando el use case emite registros`() = runTest(testDispatcher) {
        val getWasteRecordsUseCase = mockk<GetWasteRecordsUseCase>()
        every { getWasteRecordsUseCase() } returns flow {
            kotlinx.coroutines.delay(800)
            emit(records)
        }

        val viewModel = createViewModel(getWasteRecordsUseCase)

        viewModel.uiState.test {
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertTrue(loadingState.records.isEmpty())

            testDispatcher.scheduler.advanceUntilIdle()

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertEquals(records, successState.records)
            assertEquals(2, successState.totalRecords)
            assertEquals(55, successState.totalPoints)
            assertEquals(null, successState.errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filtrar por categoria deja solo esos registros y conserva el total`() = runTest(testDispatcher) {
        val viewModel = createViewModelWith(records)
        advanceUntilIdle()

        viewModel.onCategorySelected(WasteCategory.GLASS)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(glass), state.records)
        assertEquals(2, state.totalRecords)
        assertEquals(WasteCategory.GLASS, state.selectedCategory)
        assertEquals(5.0, state.filteredWeightKg, 0.0)
        assertTrue(state.hasActiveFilters)
    }

    @Test
    fun `un rango personalizado sin coincidencias deja la lista vacia`() = runTest(testDispatcher) {
        val viewModel = createViewModelWith(records) // ambos registros con recordedAt = 0 (1970)
        advanceUntilIdle()

        // 1 de enero de 2030 (medianoche UTC)
        viewModel.onCustomDateRangeSelected(1_893_456_000_000L, null)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DatePreset.CUSTOM, state.datePreset)
        assertTrue(state.records.isEmpty())
        assertEquals(2, state.totalRecords)
    }

    @Test
    fun `limpiar filtros vuelve a mostrar todos los registros`() = runTest(testDispatcher) {
        val viewModel = createViewModelWith(records)
        advanceUntilIdle()
        viewModel.onCategorySelected(WasteCategory.GLASS)
        advanceUntilIdle()

        viewModel.clearFilters()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(records, state.records)
        assertNull(state.selectedCategory)
        assertEquals(DatePreset.ALL, state.datePreset)
        assertFalse(state.hasActiveFilters)
    }

    @Test
    fun `confirmar la edicion actualiza el registro y cierra el dialogo`() = runTest(testDispatcher) {
        coEvery { updateWasteRecordUseCase(plastic, WasteCategory.GLASS, 2.5) } returns Result.success(Unit)
        val viewModel = createViewModelWith(records)
        advanceUntilIdle()

        viewModel.onEditRequested(plastic)
        assertEquals(plastic, viewModel.uiState.value.editingRecord)

        viewModel.onEditConfirmed(WasteCategory.GLASS, "2,5") // acepta coma decimal
        advanceUntilIdle()

        coVerify { updateWasteRecordUseCase(plastic, WasteCategory.GLASS, 2.5) }
        val state = viewModel.uiState.value
        assertNull(state.editingRecord)
        assertEquals("Registro actualizado", state.userMessage)
    }

    @Test
    fun `peso no numerico muestra error y no llama al caso de uso`() = runTest(testDispatcher) {
        val viewModel = createViewModelWith(records)
        advanceUntilIdle()
        viewModel.onEditRequested(plastic)

        viewModel.onEditConfirmed(WasteCategory.PLASTIC, "abc")
        advanceUntilIdle()

        assertEquals("Ingresa un peso válido", viewModel.uiState.value.editError)
        assertEquals(plastic, viewModel.uiState.value.editingRecord)
        coVerify(exactly = 0) { updateWasteRecordUseCase(any(), any(), any()) }
    }

    @Test
    fun `si la edicion falla el dialogo sigue abierto con el error`() = runTest(testDispatcher) {
        coEvery { updateWasteRecordUseCase(any(), any(), any()) } returns
                Result.failure(IllegalArgumentException("El peso debe estar entre 0.01 y 1000 kg"))
        val viewModel = createViewModelWith(records)
        advanceUntilIdle()
        viewModel.onEditRequested(plastic)

        viewModel.onEditConfirmed(WasteCategory.PLASTIC, "5000")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("El peso debe estar entre 0.01 y 1000 kg", state.editError)
        assertEquals(plastic, state.editingRecord)
    }

    @Test
    fun `cancelar la edicion cierra el dialogo sin guardar`() = runTest(testDispatcher) {
        val viewModel = createViewModelWith(records)
        advanceUntilIdle()
        viewModel.onEditRequested(plastic)

        viewModel.onEditDismissed()

        assertNull(viewModel.uiState.value.editingRecord)
        coVerify(exactly = 0) { updateWasteRecordUseCase(any(), any(), any()) }
    }

    @Test
    fun `eliminar pide confirmacion y solo borra al confirmar`() = runTest(testDispatcher) {
        coEvery { deleteWasteRecordUseCase("1") } returns Result.success(Unit)
        val viewModel = createViewModelWith(records)
        advanceUntilIdle()

        viewModel.onDeleteRequested(plastic)
        assertEquals(plastic, viewModel.uiState.value.recordPendingDeletion)
        coVerify(exactly = 0) { deleteWasteRecordUseCase(any()) }

        viewModel.onDeleteConfirmed()
        advanceUntilIdle()

        coVerify { deleteWasteRecordUseCase("1") }
        val state = viewModel.uiState.value
        assertNull(state.recordPendingDeletion)
        assertEquals("Registro eliminado", state.userMessage)
    }

    @Test
    fun `cancelar la eliminacion no borra nada`() = runTest(testDispatcher) {
        val viewModel = createViewModelWith(records)
        advanceUntilIdle()
        viewModel.onDeleteRequested(plastic)

        viewModel.onDeleteDismissed()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.recordPendingDeletion)
        coVerify(exactly = 0) { deleteWasteRecordUseCase(any()) }
    }

    @Test
    fun `clearUserMessage limpia el mensaje mostrado`() = runTest(testDispatcher) {
        coEvery { deleteWasteRecordUseCase("1") } returns Result.success(Unit)
        val viewModel = createViewModelWith(records)
        advanceUntilIdle()
        viewModel.onDeleteRequested(plastic)
        viewModel.onDeleteConfirmed()
        advanceUntilIdle()

        viewModel.clearUserMessage()

        assertNull(viewModel.uiState.value.userMessage)
    }
}