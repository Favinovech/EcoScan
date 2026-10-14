package pe.ecoscan.app.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.core.common.Resource
import pe.ecoscan.app.domain.model.CollectionPoint
import pe.ecoscan.app.domain.model.GeoPoint
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.usecase.FilterCollectionPointsUseCase
import pe.ecoscan.app.domain.usecase.GetCurrentLocationUseCase
import pe.ecoscan.app.domain.usecase.GetNearbyCollectionPointsUseCase
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    private val getNearbyCollectionPointsUseCase: GetNearbyCollectionPointsUseCase,
    private val filterCollectionPointsUseCase: FilterCollectionPointsUseCase,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    // La pantalla informa el estado del permiso al abrirse y cada vez que vuelve a primer
    // plano. La búsqueda arranca solo cuando el permiso pasa de denegado a concedido, así
    // que repetir la notificación no vuelve a consultar la API.
    fun onPermissionResult(isGranted: Boolean) {
        val wasGranted = _uiState.value.hasLocationPermission
        _uiState.update { it.copy(hasLocationPermission = isGranted) }
        if (isGranted && !wasGranted) loadNearbyPoints()
    }

    // Obtiene la ubicación y busca los puntos cercanos. También es el "Actualizar" y el
    // "Reintentar" de la pantalla.
    fun loadNearbyPoints() {
        if (!_uiState.value.hasLocationPermission) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch(dispatcherProvider.main) {
            _uiState.update { it.copy(isLoading = true, locationError = null, errorMessage = null) }

            val location = getCurrentLocationUseCase().let { result ->
                result.getOrNull() ?: run {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            locationError = result.exceptionOrNull()?.message
                                ?: "No se pudo obtener tu ubicación."
                        )
                    }
                    return@launch
                }
            }
            _uiState.update { it.copy(userLocation = location) }
            searchPoints(location)
        }
    }

    private suspend fun searchPoints(location: GeoPoint) {
        when (val resource = getNearbyCollectionPointsUseCase(location, MapUiState.SEARCH_RADIUS_METERS)) {
            is Resource.Success -> applyPoints(resource.data, isOffline = resource.isStale)
            is Resource.Error -> _uiState.update {
                it.copy(isLoading = false, errorMessage = resource.message)
            }
            Resource.Loading -> Unit
        }
    }

    private fun applyPoints(points: List<CollectionPoint>, isOffline: Boolean) {
        _uiState.update { state ->
            val visible = filterCollectionPointsUseCase(points, state.selectedMaterial, state.maxDistanceMeters)
            state.copy(
                isLoading = false,
                allPoints = points,
                visiblePoints = visible,
                isOfflineData = isOffline,
                selectedPoint = state.selectedPoint?.takeIf { it in visible }
            )
        }
    }

    // ---- Filtros (se aplican sobre los puntos ya descargados) ----

    fun onMaterialSelected(material: WasteCategory?) {
        _uiState.update { it.copy(selectedMaterial = material).refiltered() }
    }

    fun onMaxDistanceSelected(maxDistanceMeters: Int) {
        _uiState.update { it.copy(maxDistanceMeters = maxDistanceMeters).refiltered() }
    }

    private fun MapUiState.refiltered(): MapUiState {
        val visible = filterCollectionPointsUseCase(allPoints, selectedMaterial, maxDistanceMeters)
        return copy(visiblePoints = visible, selectedPoint = selectedPoint?.takeIf { it in visible })
    }

    // ---- Ficha del punto ----

    fun onPointSelected(point: CollectionPoint?) {
        _uiState.update { it.copy(selectedPoint = point) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}