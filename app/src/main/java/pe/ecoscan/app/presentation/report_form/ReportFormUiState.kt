package pe.ecoscan.app.presentation.report_form

import android.net.Uri
import pe.ecoscan.app.domain.model.GeoPoint
import pe.ecoscan.app.domain.model.ReportType

data class ReportFormUiState(
    val title: String = "",
    val description: String = "",
    val selectedCategory: String = "Plástico",
    val reportType: ReportType = ReportType.NEW_POINT,
    val imageUri: Uri? = null,
    val location: GeoPoint? = null,
    val isFetchingLocation: Boolean = false,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)