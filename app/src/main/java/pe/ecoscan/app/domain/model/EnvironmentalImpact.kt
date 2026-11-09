package pe.ecoscan.app.domain.model

/**
 * Factores de conversión estándar de impacto ambiental por kilogramo reciclado.
 */
object ImpactFactors {
    // Retorno: Pair(kg CO2 evitado por kg, Litros de agua ahorrados por kg)
    fun getFactors(category: WasteCategory): Pair<Double, Double> {
        return when (category) {
            WasteCategory.PLASTIC -> Pair(1.5, 5.7)    // 1 kg plástico = 1.5 kg CO2, 5.7 L agua
            WasteCategory.GLASS -> Pair(0.3, 1.2)      // 1 kg vidrio = 0.3 kg CO2, 1.2 L agua
            WasteCategory.PAPER -> Pair(0.9, 26.0)     // 1 kg papel = 0.9 kg CO2, 26 L agua
            WasteCategory.METAL -> Pair(4.0, 15.0)     // 1 kg metal = 4.0 kg CO2, 15 L agua
            WasteCategory.ORGANIC -> Pair(0.5, 0.0)    // 1 kg orgánico = 0.5 kg CO2 (compost)
            WasteCategory.HAZARDOUS -> Pair(2.0, 10.0) // Manejo controlado
        }
    }
}

data class ImpactMetrics(
    val totalWeightKg: Double = 0.0,
    val co2SavedKg: Double = 0.0,
    val waterSavedLiters: Double = 0.0,
    val communityAvgKg: Double = 3.5 // Promedio semanal estimado de la comunidad
)

data class MaterialDistribution(
    val category: WasteCategory,
    val totalKg: Double,
    val percentage: Float
)

data class BarChartItem(
    val label: String, // ej. "Lun", "Mar" o "Sem 1", "Sem 2"
    val weightKg: Float
)

enum class TimeRangeFilter {
    WEEKLY,
    MONTHLY
}