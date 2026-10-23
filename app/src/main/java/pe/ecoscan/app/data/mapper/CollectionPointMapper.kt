package pe.ecoscan.app.data.mapper

import pe.ecoscan.app.data.local.entity.CachedCollectionPointEntity
import pe.ecoscan.app.data.remote.dto.OverpassElementDto
import pe.ecoscan.app.domain.model.CollectionPoint
import pe.ecoscan.app.domain.model.GeoPoint
import pe.ecoscan.app.domain.model.WasteCategory
import kotlin.math.roundToInt

private const val MATERIALS_SEPARATOR = "|"

// Etiquetas de OpenStreetMap (recycling:*=yes) que corresponden a cada categoría de EcoScan.
private val MATERIAL_TAGS: Map<WasteCategory, List<String>> = mapOf(
    WasteCategory.PLASTIC to listOf(
        "recycling:plastic", "recycling:plastic_bottles", "recycling:plastic_packaging"
    ),
    WasteCategory.GLASS to listOf(
        "recycling:glass", "recycling:glass_bottles"
    ),
    WasteCategory.PAPER to listOf(
        "recycling:paper", "recycling:cardboard", "recycling:newspaper", "recycling:magazines"
    ),
    WasteCategory.METAL to listOf(
        "recycling:metal", "recycling:scrap_metal", "recycling:cans", "recycling:aluminium"
    ),
    WasteCategory.ORGANIC to listOf(
        "recycling:organic", "recycling:green_waste", "recycling:garden_waste", "recycling:food_waste"
    ),
    WasteCategory.HAZARDOUS to listOf(
        "recycling:batteries", "recycling:car_batteries", "recycling:electrical_items",
        "recycling:small_electrical_appliances", "recycling:chemicals", "recycling:paint",
        "recycling:engine_oil", "recycling:cooking_oil", "recycling:light_bulbs",
        "recycling:fluorescent_tubes"
    )
)

// Convierte un elemento de Overpass en un punto de acopio. Devuelve null si el elemento no
// tiene id o coordenadas, porque sin ellas no se puede mostrar en el mapa.
// origin es el lugar de la búsqueda, para calcular la distancia.
fun OverpassElementDto.toDomain(origin: GeoPoint): CollectionPoint? {
    val latitude = lat ?: center?.lat ?: return null
    val longitude = lon ?: center?.lon ?: return null
    val elementId = id ?: return null
    val osmTags = tags.orEmpty()
    val location = GeoPoint(latitude, longitude)

    return CollectionPoint(
        id = "${type ?: "node"}/$elementId",
        name = osmTags["name"].cleaned() ?: osmTags["operator"].cleaned(),
        location = location,
        address = osmTags.toAddress(),
        openingHours = osmTags["opening_hours"].cleaned(),
        acceptedMaterials = osmTags.toMaterials(),
        distanceMeters = origin.distanceTo(location).roundToInt()
    )
}

fun CollectionPoint.toEntity(cachedAt: Long): CachedCollectionPointEntity = CachedCollectionPointEntity(
    id = id,
    name = name,
    latitude = location.latitude,
    longitude = location.longitude,
    address = address,
    openingHours = openingHours,
    acceptedMaterials = acceptedMaterials.joinToString(MATERIALS_SEPARATOR) { it.name },
    cachedAt = cachedAt
)

fun CachedCollectionPointEntity.toDomain(origin: GeoPoint): CollectionPoint {
    val location = GeoPoint(latitude, longitude)
    return CollectionPoint(
        id = id,
        name = name,
        location = location,
        address = address,
        openingHours = openingHours,
        acceptedMaterials = acceptedMaterials
            .split(MATERIALS_SEPARATOR)
            .mapNotNull { stored -> WasteCategory.entries.firstOrNull { it.name == stored } }
            .toSet(),
        distanceMeters = origin.distanceTo(location).roundToInt()
    )
}

private fun String?.cleaned(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

private fun Map<String, String>.toMaterials(): Set<WasteCategory> =
    MATERIAL_TAGS.filter { (_, keys) -> keys.any { this[it] == "yes" } }.keys

// Arma la dirección con las etiquetas addr:* de OSM. Si no hay ninguna, devuelve null.
private fun Map<String, String>.toAddress(): String? {
    this["addr:full"].cleaned()?.let { return it }
    val street = listOfNotNull(this["addr:street"].cleaned(), this["addr:housenumber"].cleaned())
        .joinToString(" ")
    val area = this["addr:suburb"].cleaned() ?: this["addr:district"].cleaned() ?: this["addr:city"].cleaned()
    return listOfNotNull(street.takeIf { it.isNotEmpty() }, area).joinToString(", ").takeIf { it.isNotEmpty() }
}