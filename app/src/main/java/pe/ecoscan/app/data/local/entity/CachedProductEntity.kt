package pe.ecoscan.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Fila de la tabla "cached_products": resultado cacheado de una consulta a Open Food
// Facts, para no repetir la llamada de red por el mismo código de barras.
// packagingMaterials se guarda como texto separado por "|" porque Room no soporta listas
// sin un TypeConverter; se reconstruye en ProductMapper.
@Entity(tableName = "cached_products")
data class CachedProductEntity(
    @PrimaryKey
    val barcode: String,
    val name: String?,
    val brand: String?,
    val packagingMaterials: String,
    val imageUrl: String?,
    val disposalHint: String,
    val cachedAt: Long
)
