package pe.ecoscan.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// Envoltorio de la respuesta de GET product/{barcode}.json de Open Food Facts.
// status = 1 si el producto existe, 0 si no se encontró.
data class ProductResponseDto(
    @SerializedName("status")
    val status: Int,
    @SerializedName("code")
    val code: String? = null,
    @SerializedName("product")
    val product: ProductDto? = null
)
