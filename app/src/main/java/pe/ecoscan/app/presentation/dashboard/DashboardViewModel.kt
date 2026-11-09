package pe.ecoscan.app.presentation.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import pe.ecoscan.app.core.common.ReportExporter
import pe.ecoscan.app.domain.model.TimeRangeFilter
import pe.ecoscan.app.domain.usecase.GetEnvironmentalImpactUseCase
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getEnvironmentalImpactUseCase: GetEnvironmentalImpactUseCase
) : ViewModel() {

    private val _selectedTimeRange = MutableStateFlow(TimeRangeFilter.WEEKLY)
    val selectedTimeRange: StateFlow<TimeRangeFilter> = _selectedTimeRange.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = _selectedTimeRange
        .flatMapLatest { timeRange ->
            getEnvironmentalImpactUseCase(timeRange)
        }
        .map { dashboardData ->
            DashboardUiState.Success(
                data = dashboardData,
                selectedTimeRange = _selectedTimeRange.value
            ) as DashboardUiState
        }
        .catch { throwable ->
            emit(DashboardUiState.Error(throwable.message ?: "Error al cargar estadísticas"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState.Loading
        )

    fun onTimeRangeSelected(range: TimeRangeFilter) {
        _selectedTimeRange.value = range
    }

    fun exportAndShareReport(context: Context) {
        val currentState = uiState.value
        if (currentState is DashboardUiState.Success) {
            ReportExporter.exportToCsvAndShare(context, currentState.data)
        }
    }
}