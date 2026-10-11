package pe.ecoscan.app.presentation.history

import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteRecord

data class HistoryUiState(
    val isLoading: Boolean = true,
    // Registros que se muestran: ya filtrados por categoría y fechas.
    val records: List<WasteRecord> = emptyList(),
    // Cantidad de registros sin filtrar, para distinguir "no hay nada" de "el filtro no encontró nada".
    val totalRecords: Int = 0,
    // EcoPuntos de todos los registros, sin filtrar.
    val totalPoints: Int = 0,
    val filteredWeightKg: Double = 0.0,
    val selectedCategory: WasteCategory? = null,
    val datePreset: DatePreset = DatePreset.ALL,
    val customRange: DateRange? = null,
    // Edición y eliminación con confirmación.
    val editingRecord: WasteRecord? = null,
    val editError: String? = null,
    val recordPendingDeletion: WasteRecord? = null,
    // Mensaje de una sola vez (Snackbar): "Registro actualizado", errores, etc.
    val userMessage: String? = null,
    val errorMessage: String? = null
) {
    val hasActiveFilters: Boolean
        get() = selectedCategory != null || datePreset != DatePreset.ALL
}