package pe.ecoscan.app.domain.model

// Categorías de residuos reciclables que maneja un punto de acopio.
enum class WasteCategory(val displayName: String) {
    PLASTIC("Plástico"),
    GLASS("Vidrio"),
    PAPER("Papel"),
    METAL("Metal"),
    ORGANIC("Orgánico"),
    HAZARDOUS("Peligroso")
}
