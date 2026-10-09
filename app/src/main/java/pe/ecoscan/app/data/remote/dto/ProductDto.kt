package pe.ecoscan.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// Datos del producto dentro de la respuesta de Open Food Facts. Todos los campos son
// opcionales: Open Food Facts puede omitir cualquiera según qué tan completa esté la
// ficha del producto.
data class ProductDto(
    @SerializedName("product_name")
    val productName: String? = null,
    @SerializedName("brands")
    val brands: String? = null,
    @SerializedName("packaging")
    val packaging: String? = null,
    @SerializedName("packaging_tags")
    val packagingTags: List<String>? = null,
    @SerializedName("categories_tags")
    val categoriesTags: List<String>? = null,
    @SerializedName("image_front_small_url")
    val imageFrontSmallUrl: String? = null
)
