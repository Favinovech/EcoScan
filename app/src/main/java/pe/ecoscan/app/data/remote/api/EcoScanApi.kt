package pe.ecoscan.app.data.remote.api

import pe.ecoscan.app.data.remote.dto.ProductResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// Open Food Facts: resuelve un producto escaneado por su código de barras (HU05).
interface EcoScanApi {

    @GET("product/{barcode}.json")
    suspend fun getProductByBarcode(
        @Path("barcode") barcode: String,
        @Query("fields") fields: String
    ): ProductResponseDto

    companion object {
        // Limita la respuesta a lo que PackagedProduct necesita, para aligerar la carga.
        const val PRODUCT_FIELDS =
            "product_name,brands,packaging,packaging_tags,categories_tags,image_front_small_url"
    }
}
