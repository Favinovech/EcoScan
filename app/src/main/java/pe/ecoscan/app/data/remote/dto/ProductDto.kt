package pe.ecoscan.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// Plantilla de DTO para el Sprint 3, cuando se consulte Open Food Facts por código de barras.
data class ProductDto(
    @SerializedName("code")
    val code: String,
    @SerializedName("product_name")
    val productName: String?,
    @SerializedName("packaging")
    val packaging: String?
)
