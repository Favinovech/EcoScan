package pe.ecoscan.app.domain.model

// Resultado de la clasificación por IA (HU04).
data class WasteClassification(
    val category: WasteCategory,
    val confidence: Float,
    val instructions: String,
    val recommendedContainerColor: String
)