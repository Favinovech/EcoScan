package pe.ecoscan.app.data.remote.api

import pe.ecoscan.app.data.remote.dto.ProductDto
import retrofit2.http.GET
import retrofit2.http.Path

// Interfaz de plantilla: en el Sprint 3 se conecta de verdad a Open Food Facts para
// resolver el código de barras escaneado a un producto reciclable.
interface EcoScanApi {

    @GET("api/v0/product/{barcode}.json")
    suspend fun getProductByBarcode(@Path("barcode") barcode: String): ProductDto
}
