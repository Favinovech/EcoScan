package pe.ecoscan.app.domain.model

// Producto consultado por código de barras en Open Food Facts, ya resuelto a los
// conceptos de reciclaje que usa la app.
data class PackagedProduct(
    val barcode: String,
    val name: String?,
    val brand: String?,
    val packagingMaterials: List<String>,
    val imageUrl: String?,
    val disposalHint: String
)
