package pe.ecoscan.app.presentation.scan

import android.graphics.Bitmap
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteClassification

data class ScanUiState(
    val isAnalyzing: Boolean = false,
    val isSaving: Boolean = false,
    val capturedBitmap: Bitmap? = null,
    val classification: WasteClassification? = null,
    val selectedCategory: WasteCategory? = null,
    val estimatedWeightKg: Double = 0.5,
    val isSavedSuccess: Boolean = false,
    val errorMessage: String? = null,
    val hasCameraPermission: Boolean = false,
    val detectedBarcode: String? = null
)