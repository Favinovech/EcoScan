package pe.ecoscan.app.domain.repository

import pe.ecoscan.app.core.common.Resource
import pe.ecoscan.app.domain.model.PackagedProduct

interface ProductRepository {
    suspend fun getProductByBarcode(barcode: String): Resource<PackagedProduct>
}
