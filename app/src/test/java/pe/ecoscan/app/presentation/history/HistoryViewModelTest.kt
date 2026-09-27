package pe.ecoscan.app.presentation.history

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteRecord
import pe.ecoscan.app.domain.usecase.GetTotalPointsUseCase
import pe.ecoscan.app.domain.usecase.GetWasteRecordsUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeDispatcherProvider = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState transiciona de carga a datos cuando el use case emite registros`() = runTest(testDispatcher) {
        val fakeRecords = listOf(
            WasteRecord("1", WasteCategory.PLASTIC, weightKg = 2.5, points = 25, recordedAt = 0L),
            WasteRecord("2", WasteCategory.GLASS, weightKg = 5.0, points = 30, recordedAt = 0L)
        )

        val getWasteRecordsUseCase = mockk<GetWasteRecordsUseCase>()
        every { getWasteRecordsUseCase() } returns flow {
            kotlinx.coroutines.delay(800)
            emit(fakeRecords)
        }

        val getTotalPointsUseCase = mockk<GetTotalPointsUseCase>()
        every { getTotalPointsUseCase(fakeRecords) } returns 55

        val viewModel = HistoryViewModel(
            getWasteRecordsUseCase = getWasteRecordsUseCase,
            getTotalPointsUseCase = getTotalPointsUseCase,
            dispatcherProvider = fakeDispatcherProvider
        )

        viewModel.uiState.test {
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)
            assertTrue(loadingState.records.isEmpty())

            testDispatcher.scheduler.advanceUntilIdle()

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertEquals(fakeRecords, successState.records)
            assertEquals(55, successState.totalPoints)
            assertEquals(null, successState.errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
