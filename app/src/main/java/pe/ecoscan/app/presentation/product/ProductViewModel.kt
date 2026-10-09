package pe.ecoscan.app.presentation.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.core.common.Resource
import pe.ecoscan.app.domain.usecase.GetProductByBarcodeUseCase
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getProductByBarcodeUseCase: GetProductByBarcodeUseCase,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private var lastBarcode: String? = null

    fun consultar(barcode: String) {
        lastBarcode = barcode
        viewModelScope.launch(dispatcherProvider.main) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val resource = getProductByBarcodeUseCase(barcode)) {
                is Resource.Loading -> Unit
                is Resource.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        product = resource.data,
                        error = null,
                        isFromCache = resource.isStale
                    )
                }

                is Resource.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        product = null,
                        error = resource.type,
                        isFromCache = false
                    )
                }
            }
        }
    }

    fun reintentar() {
        lastBarcode?.let { consultar(it) }
    }
}
