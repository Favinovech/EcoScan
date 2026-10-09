package pe.ecoscan.app.domain.usecase

import pe.ecoscan.app.core.common.Resource
import pe.ecoscan.app.domain.model.PackagedProduct
import pe.ecoscan.app.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductByBarcodeUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(barcode: String): Resource<PackagedProduct> =
        repository.getProductByBarcode(barcode)
}
