package pe.ecoscan.app.presentation.dashboard

import pe.ecoscan.app.domain.model.TimeRangeFilter
import pe.ecoscan.app.domain.usecase.DashboardData

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(
        val data: DashboardData,
        val selectedTimeRange: TimeRangeFilter = TimeRangeFilter.WEEKLY
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}