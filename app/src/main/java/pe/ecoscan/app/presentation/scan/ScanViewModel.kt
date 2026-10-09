package pe.ecoscan.app.presentation.scan

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.ecoscan.app.core.common.DefaultDispatcherProvider
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.data.repository.WasteRecordRepositoryImpl
import pe.ecoscan.app.data.scanner.BarcodeDetector
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteDisposalInfo
import pe.ecoscan.app.domain.model.WasteRecord
import pe.ecoscan.app.domain.usecase.ClassifyWasteUseCase
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val classifyWasteUseCase: ClassifyWasteUseCase,
    private val wasteRecordRepository: WasteRecordRepositoryImpl,
    private val dispatcherProvider: DispatcherProvider,
    private val barcodeDetector: BarcodeDetector = BarcodeDetector(DefaultDispatcherProvider())
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun onPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = isGranted) }
    }

    fun onImageCaptured(bitmap: Bitmap) {
        viewModelScope.launch(dispatcherProvider.main) {
            _uiState.update {
                it.copy(
                    isAnalyzing = true,
                    capturedBitmap = bitmap,
                    errorMessage = null,
                    detectedBarcode = null
                )
            }
            val result = classifyWasteUseCase(bitmap)
            result.onSuccess { classification ->
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        classification = classification,
                        selectedCategory = classification.category
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = error.localizedMessage ?: "Error al clasificar residuo"
                    )
                }
            }

            barcodeDetector.detect(bitmap).onSuccess { barcode ->
                if (barcode != null) {
                    _uiState.update { it.copy(detectedBarcode = barcode) }
                }
            }
        }
    }

    fun onCategoryCorrected(newCategory: WasteCategory) {
        val currentClassification = _uiState.value.classification ?: return
        val updatedClassification = currentClassification.copy(
            category = newCategory,
            instructions = WasteDisposalInfo.getInstructions(newCategory),
            recommendedContainerColor = WasteDisposalInfo.getContainerColorName(newCategory)
        )
        _uiState.update {
            it.copy(
                selectedCategory = newCategory,
                classification = updatedClassification
            )
        }
    }

    fun onWeightChanged(weight: Double) {
        _uiState.update { it.copy(estimatedWeightKg = weight) }
    }

    fun saveToHistory() {
        val category = _uiState.value.selectedCategory ?: return
        val weight = _uiState.value.estimatedWeightKg
        val points = (WasteDisposalInfo.getDefaultPoints(category) * weight).toInt().coerceAtLeast(1)

        viewModelScope.launch(dispatcherProvider.main) {
            _uiState.update { it.copy(isSaving = true) }
            val record = WasteRecord(
                id = "0",
                category = category,
                weightKg = weight,
                points = points,
                recordedAt = System.currentTimeMillis()
            )
            wasteRecordRepository.insert(record)
            _uiState.update { it.copy(isSaving = false, isSavedSuccess = true) }
        }
    }

    fun resetScan() {
        _uiState.update {
            it.copy(
                capturedBitmap = null,
                classification = null,
                selectedCategory = null,
                isSavedSuccess = false,
                errorMessage = null,
                detectedBarcode = null
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}