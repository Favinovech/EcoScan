package pe.ecoscan.app.domain.model

// Registro de un depósito de residuos hecho por un usuario en un punto de acopio.
// recordedAt se guarda en epoch millis: el dominio no depende de tipos de fecha de la
// plataforma, el formateo a texto legible se hace en la capa de presentación.
// isSynced indica si el registro ya está reflejado en Firestore (HU06, offline-first).
data class WasteRecord(
    val id: String,
    val category: WasteCategory,
    val weightKg: Double,
    val points: Int,
    val recordedAt: Long,
    val isSynced: Boolean = false
) {
    companion object {
        const val MIN_WEIGHT_KG = 0.01
        const val MAX_WEIGHT_KG = 1000.0
    }
}