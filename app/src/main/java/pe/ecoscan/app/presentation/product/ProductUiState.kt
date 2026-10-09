package pe.ecoscan.app.presentation.product

import pe.ecoscan.app.core.common.ErrorType
import pe.ecoscan.app.domain.model.PackagedProduct

data class ProductUiState(
    val isLoading: Boolean = false,
    val product: PackagedProduct? = null,
    val error: ErrorType? = null,
    val isFromCache: Boolean = false
)
