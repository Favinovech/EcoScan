package pe.ecoscan.app.presentation.history

import pe.ecoscan.app.domain.model.WasteRecord

data class HistoryUiState(
    val isLoading: Boolean = true,
    val records: List<WasteRecord> = emptyList(),
    val totalPoints: Int = 0,
    val errorMessage: String? = null
)
