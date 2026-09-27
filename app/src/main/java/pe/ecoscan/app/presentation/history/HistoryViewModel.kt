package pe.ecoscan.app.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.core.common.Resource
import pe.ecoscan.app.core.common.asResource
import pe.ecoscan.app.domain.model.WasteRecord
import pe.ecoscan.app.domain.usecase.GetTotalPointsUseCase
import pe.ecoscan.app.domain.usecase.GetWasteRecordsUseCase
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getWasteRecordsUseCase: GetWasteRecordsUseCase,
    private val getTotalPointsUseCase: GetTotalPointsUseCase,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        observeWasteRecords()
    }

    private fun observeWasteRecords() {
        getWasteRecordsUseCase()
            .asResource()
            .onEach { resource -> _uiState.update { it.reduce(resource) } }
            .flowOn(dispatcherProvider.io)
            .launchIn(viewModelScope)
    }

    private fun HistoryUiState.reduce(resource: Resource<List<WasteRecord>>) =
        when (resource) {
            is Resource.Loading -> copy(isLoading = true, errorMessage = null)
            is Resource.Success -> copy(
                isLoading = false,
                records = resource.data,
                totalPoints = getTotalPointsUseCase(resource.data),
                errorMessage = null
            )
            is Resource.Error -> copy(isLoading = false, errorMessage = resource.message)
        }
}
