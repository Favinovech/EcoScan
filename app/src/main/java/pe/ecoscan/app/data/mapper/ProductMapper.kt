package pe.ecoscan.app.data.mapper

import pe.ecoscan.app.data.local.entity.CachedProductEntity
import pe.ecoscan.app.data.remote.dto.ProductDto
import pe.ecoscan.app.domain.model.PackagedProduct
import pe.ecoscan.app.domain.model.PackagingMaterialMapper

private const val MATERIALS_DELIMITER = "|"

// El dominio no conoce ni los DTO de Retrofit ni las entidades de Room: toda la
// conversión vive aquí.
fun ProductDto.toDomain(barcode: String): PackagedProduct {
    val resolution = PackagingMaterialMapper.resolve(packaging, packagingTags.orEmpty())
    return PackagedProduct(
        barcode = barcode,
        name = productName,
        brand = brands,
        packagingMaterials = resolution.packagingMaterials,
        imageUrl = imageFrontSmallUrl,
        disposalHint = resolution.disposalHint
    )
}

fun PackagedProduct.toEntity(cachedAt: Long): CachedProductEntity = CachedProductEntity(
    barcode = barcode,
    name = name,
    brand = brand,
    packagingMaterials = packagingMaterials.joinToString(MATERIALS_DELIMITER),
    imageUrl = imageUrl,
    disposalHint = disposalHint,
    cachedAt = cachedAt
)

fun CachedProductEntity.toDomain(): PackagedProduct = PackagedProduct(
    barcode = barcode,
    name = name,
    brand = brand,
    packagingMaterials = if (packagingMaterials.isEmpty()) {
        emptyList()
    } else {
        packagingMaterials.split(MATERIALS_DELIMITER)
    },
    imageUrl = imageUrl,
    disposalHint = disposalHint
)
