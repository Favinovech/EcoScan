package pe.ecoscan.app.domain.model

// Traduce las etiquetas "packaging"/"packaging_tags" de Open Food Facts (texto libre,
// en varios idiomas) a las categorías de residuo que ya maneja la app y genera la
// instrucción de separación correspondiente. Una etiqueta no reconocida no es un error:
// se reporta con una indicación neutra para que el usuario decida en el punto de acopio.
object PackagingMaterialMapper {

    data class MaterialResolution(
        val packagingMaterials: List<String>,
        val disposalHint: String
    )

    private const val NEUTRAL_HINT =
        "No se reconoció el material de empaque. Consulta en el punto de acopio cómo separarlo."

    private const val TETRAPACK_LABEL = "Tetrapak"
    private const val TETRAPACK_HINT =
        "Es un envase compuesto (cartón, plástico y aluminio): sepáralo como tetrapak y " +
            "llévalo a un punto de acopio que reciba envases compuestos."

    private val TETRAPACK_PATTERN = Regex("tetra.?pak|tetra.?brik|carton.?brik", RegexOption.IGNORE_CASE)

    private val CATEGORY_PATTERNS: List<Pair<Regex, WasteCategory>> = listOf(
        Regex(
            "plastic|pl[aá]stico|\\bpet\\b|\\bhdpe\\b|\\bldpe\\b|\\bpp\\b|\\bpvc\\b|polypropylene|polyethylene",
            RegexOption.IGNORE_CASE
        ) to WasteCategory.PLASTIC,
        Regex("glass|vidrio|verre|\\bglas\\b", RegexOption.IGNORE_CASE) to WasteCategory.GLASS,
        Regex("paper|papel|cardboard|cart[oó]n|papier|karton", RegexOption.IGNORE_CASE) to WasteCategory.PAPER,
        Regex(
            "metal|aluminum|aluminio|aluminium|steel|acero|\\btin\\b|lata|hojalata",
            RegexOption.IGNORE_CASE
        ) to WasteCategory.METAL
    )

    private val CATEGORY_LABELS: Map<WasteCategory, String> = mapOf(
        WasteCategory.PLASTIC to "Plástico",
        WasteCategory.GLASS to "Vidrio",
        WasteCategory.PAPER to "Papel y cartón",
        WasteCategory.METAL to "Metal y aluminio"
    )

    private val CATEGORY_HINTS: Map<WasteCategory, String> = mapOf(
        WasteCategory.PLASTIC to "Enjuaga el envase y sepáralo como plástico.",
        WasteCategory.GLASS to "Sepáralo como vidrio; evita mezclarlo con cerámica o espejos.",
        WasteCategory.PAPER to "Sepáralo como papel y cartón; evita que esté mojado o engrasado.",
        WasteCategory.METAL to "Sepáralo como metal y aluminio; aplástalo si es posible para ahorrar espacio."
    )

    fun resolve(packagingFreeText: String?, packagingTags: List<String>): MaterialResolution {
        val tokens = buildList {
            packagingFreeText?.let { add(it) }
            addAll(packagingTags)
        }

        val materials = linkedSetOf<String>()
        val hints = linkedSetOf<String>()

        tokens.forEach { token ->
            if (TETRAPACK_PATTERN.containsMatchIn(token)) {
                materials += TETRAPACK_LABEL
                hints += TETRAPACK_HINT
            }
            CATEGORY_PATTERNS.forEach { (pattern, category) ->
                if (pattern.containsMatchIn(token)) {
                    materials += CATEGORY_LABELS.getValue(category)
                    hints += CATEGORY_HINTS.getValue(category)
                }
            }
        }

        if (materials.isEmpty()) {
            return MaterialResolution(packagingMaterials = emptyList(), disposalHint = NEUTRAL_HINT)
        }

        return MaterialResolution(
            packagingMaterials = materials.toList(),
            disposalHint = hints.joinToString(" ")
        )
    }
}
