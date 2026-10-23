package pe.ecoscan.app.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.core.common.Resource
import pe.ecoscan.app.core.common.asResource
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteRecord
import pe.ecoscan.app.domain.model.WasteRecordFilter
import pe.ecoscan.app.domain.usecase.DeleteWasteRecordUseCase
import pe.ecoscan.app.domain.usecase.FilterWasteRecordsUseCase
import pe.ecoscan.app.domain.usecase.GetTotalPointsUseCase
import pe.ecoscan.app.domain.usecase.GetWasteRecordsUseCase
import pe.ecoscan.app.domain.usecase.UpdateWasteRecordUseCase
import javax.inject.Inject

// Lo que el usuario eligió en los filtros (separado del resultado para poder recalcular).
private data class FilterSelection(
    val category: WasteCategory? = null,
    val preset: DatePreset = DatePreset.ALL,
    val customRange: DateRange? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getWasteRecordsUseCase: GetWasteRecordsUseCase,
    private val getTotalPointsUseCase: GetTotalPointsUseCase,
    private val filterWasteRecordsUseCase: FilterWasteRecordsUseCase,
    private val updateWasteRecordUseCase: UpdateWasteRecordUseCase,
    private val deleteWasteRecordUseCase: DeleteWasteRecordUseCase,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val filterSelection = MutableStateFlow(FilterSelection())

    init {
        observeWasteRecords()
    }

    private fun observeWasteRecords() {
        combine(getWasteRecordsUseCase().asResource(), filterSelection) { resource, selection ->
            resource to selection
        }
            .onEach { (resource, selection) -> _uiState.update { it.reduce(resource, selection) } }
            .flowOn(dispatcherProvider.io)
            .launchIn(viewModelScope)
    }

    private fun HistoryUiState.reduce(
        resource: Resource<List<WasteRecord>>,
        selection: FilterSelection
    ): HistoryUiState = when (resource) {
        is Resource.Loading -> copy(isLoading = true, errorMessage = null)
        is Resource.Success -> {
            val filtered = filterWasteRecordsUseCase(resource.data, selection.toFilter())
            copy(
                isLoading = false,
                records = filtered,
                totalRecords = resource.data.size,
                totalPoints = getTotalPointsUseCase(resource.data),
                filteredWeightKg = filtered.sumOf { it.weightKg },
                selectedCategory = selection.category,
                datePreset = selection.preset,
                customRange = selection.customRange,
                errorMessage = null
            )
        }
        is Resource.Error -> copy(isLoading = false, errorMessage = resource.message)
    }

    private fun FilterSelection.toFilter(): WasteRecordFilter {
        val range = when (preset) {
            DatePreset.ALL -> null
            DatePreset.LAST_7_DAYS -> lastDaysRange(7, System.currentTimeMillis())
            DatePreset.LAST_30_DAYS -> lastDaysRange(30, System.currentTimeMillis())
            DatePreset.CUSTOM -> customRange
        }
        return WasteRecordFilter(
            category = category,
            fromMillis = range?.fromMillis,
            toMillis = range?.toMillis
        )
    }

    // ---- Filtros ----

    fun onCategorySelected(category: WasteCategory?) {
        filterSelection.update { it.copy(category = category) }
    }

    fun onDatePresetSelected(preset: DatePreset) {
        filterSelection.update { it.copy(preset = preset) }
    }

    // Rango elegido en el selector de fechas (medianoche UTC de cada día; ver pickerRangeToLocal).
    fun onCustomDateRangeSelected(startUtcMillis: Long, endUtcMillis: Long?) {
        filterSelection.update {
            it.copy(
                preset = DatePreset.CUSTOM,
                customRange = pickerRangeToLocal(startUtcMillis, endUtcMillis)
            )
        }
    }

    fun clearFilters() {
        filterSelection.value = FilterSelection()
    }

    // ---- Edición con confirmación ----

    fun onEditRequested(record: WasteRecord) {
        _uiState.update { it.copy(editingRecord = record, editError = null) }
    }

    fun onEditDismissed() {
        _uiState.update { it.copy(editingRecord = null, editError = null) }
    }

    fun onEditConfirmed(category: WasteCategory, weightText: String) {
        val record = _uiState.value.editingRecord ?: return
        val weight = weightText.trim().replace(',', '.').toDoubleOrNull()
        if (weight == null) {
            _uiState.update { it.copy(editError = "Ingresa un peso válido") }
            return
        }
        viewModelScope.launch(dispatcherProvider.main) {
            updateWasteRecordUseCase(record, category, weight)
                .onSuccess {
                    _uiState.update {
                        it.copy(editingRecord = null, editError = null, userMessage = "Registro actualizado")
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(editError = error.message ?: "No se pudo actualizar el registro")
                    }
                }
        }
    }

    // ---- Eliminación con confirmación ----

    fun onDeleteRequested(record: WasteRecord) {
        _uiState.update { it.copy(recordPendingDeletion = record) }
    }

    fun onDeleteDismissed() {
        _uiState.update { it.copy(recordPendingDeletion = null) }
    }

    fun onDeleteConfirmed() {
        val record = _uiState.value.recordPendingDeletion ?: return
        viewModelScope.launch(dispatcherProvider.main) {
            deleteWasteRecordUseCase(record.id)
                .onSuccess {
                    _uiState.update {
                        it.copy(recordPendingDeletion = null, userMessage = "Registro eliminado")
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            recordPendingDeletion = null,
                            userMessage = error.message ?: "No se pudo eliminar el registro"
                        )
                    }
                }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }
}