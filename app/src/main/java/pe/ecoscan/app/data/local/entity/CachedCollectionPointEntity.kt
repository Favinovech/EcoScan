package pe.ecoscan.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Fila de la tabla "cached_collection_points": puntos de acopio consultados recientemente,
// guardados para poder mostrarlos sin conexión (HU07).
// acceptedMaterials se guarda como nombres de WasteCategory separados por "|" porque Room
// no soporta listas sin un TypeConverter; se reconstruye en CollectionPointMapper.
@Entity(tableName = "cached_collection_points")
data class CachedCollectionPointEntity(
    @PrimaryKey
    val id: String,
    val name: String?,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val openingHours: String?,
    val acceptedMaterials: String,
    val cachedAt: Long
)