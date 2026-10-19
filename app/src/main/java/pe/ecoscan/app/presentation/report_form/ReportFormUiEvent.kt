package pe.ecoscan.app.presentation.report_form

import android.net.Uri
import pe.ecoscan.app.domain.model.ReportType

sealed interface ReportFormUiEvent {
    data class TitleChanged(val value: String) : ReportFormUiEvent
    data class DescriptionChanged(val value: String) : ReportFormUiEvent
    data class CategorySelected(val value: String) : ReportFormUiEvent
    data class ReportTypeSelected(val value: ReportType) : ReportFormUiEvent
    data class ImageSelected(val uri: Uri?) : ReportFormUiEvent
    data object RequestLocation : ReportFormUiEvent
    data object SubmitReport : ReportFormUiEvent
    data object DismissError : ReportFormUiEvent
}