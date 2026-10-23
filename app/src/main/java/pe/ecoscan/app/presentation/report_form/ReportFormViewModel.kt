package pe.ecoscan.app.presentation.report_form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.ecoscan.app.domain.model.CommunityReport
import pe.ecoscan.app.domain.repository.CommunityReportRepository
import pe.ecoscan.app.domain.repository.LocationRepository
import javax.inject.Inject

@HiltViewModel
class ReportFormViewModel @Inject constructor(
    private val repository: CommunityReportRepository,
    private val locationRepository: LocationRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportFormUiState())
    val uiState: StateFlow<ReportFormUiState> = _uiState.asStateFlow()

    init {
        captureCurrentLocation()
    }

    fun onEvent(event: ReportFormUiEvent) {
        when (event) {
            is ReportFormUiEvent.TitleChanged -> {
                _uiState.update { it.copy(title = event.value) }
            }
            is ReportFormUiEvent.DescriptionChanged -> {
                _uiState.update { it.copy(description = event.value) }
            }
            is ReportFormUiEvent.CategorySelected -> {
                _uiState.update { it.copy(selectedCategory = event.value) }
            }
            is ReportFormUiEvent.ReportTypeSelected -> {
                _uiState.update { it.copy(reportType = event.value) }
            }
            is ReportFormUiEvent.ImageSelected -> {
                _uiState.update { it.copy(imageUri = event.uri) }
            }
            is ReportFormUiEvent.RequestLocation -> {
                captureCurrentLocation()
            }
            is ReportFormUiEvent.SubmitReport -> {
                submitReport()
            }
            is ReportFormUiEvent.DismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun captureCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingLocation = true) }
            locationRepository.getCurrentLocation()
                .onSuccess { geoPoint ->
                    _uiState.update {
                        it.copy(
                            location = geoPoint,
                            isFetchingLocation = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isFetchingLocation = false,
                            errorMessage = error.localizedMessage ?: "No se pudo obtener la ubicación GPS"
                        )
                    }
                }
        }
    }

    private fun submitReport() {
        val state = _uiState.value
        val userId = auth.currentUser?.uid

        if (userId == null) {
            _uiState.update { it.copy(errorMessage = "Debes iniciar sesión para enviar un reporte") }
            return
        }
        if (state.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El título o nombre del punto es obligatorio") }
            return
        }
        val loc = state.location
        if (loc == null) {
            _uiState.update { it.copy(errorMessage = "La ubicación GPS es requerida. Pulsa en actualizar ubicación.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val newReport = CommunityReport(
                userId = userId,
                title = state.title.trim(),
                description = state.description.trim(),
                category = state.selectedCategory,
                latitude = loc.latitude,
                longitude = loc.longitude,
                reportType = state.reportType
            )

            val result = repository.createReport(newReport, state.imageUri)

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.localizedMessage ?: "Error al guardar el reporte"
                    )
                }
            }
        }
    }
}