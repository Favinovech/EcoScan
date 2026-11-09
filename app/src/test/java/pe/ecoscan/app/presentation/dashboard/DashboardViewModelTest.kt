package pe.ecoscan.app.presentation.dashboard

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.ecoscan.app.domain.model.ImpactMetrics
import pe.ecoscan.app.domain.model.TimeRangeFilter
import pe.ecoscan.app.domain.usecase.DashboardData
import pe.ecoscan.app.domain.usecase.GetEnvironmentalImpactUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val useCase = mockk<GetEnvironmentalImpactUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState emite Success cuando el caso de uso devuelve datos de impacto`() = runTest {
        val fakeData = DashboardData(
            metrics = ImpactMetrics(totalWeightKg = 5.0, co2SavedKg = 7.5, waterSavedLiters = 28.5),
            materialDistribution = emptyList(),
            barChartData = emptyList()
        )
        every { useCase(TimeRangeFilter.WEEKLY) } returns flowOf(fakeData)

        val viewModel = DashboardViewModel(useCase)

        viewModel.uiState.test {
            assertEquals(DashboardUiState.Loading, awaitItem())
            advanceUntilIdle()
            val state = awaitItem()
            assertTrue(state is DashboardUiState.Success)
            assertEquals(5.0, (state as DashboardUiState.Success).data.metrics.totalWeightKg, 0.01)
            cancelAndIgnoreRemainingEvents()
        }
    }
}