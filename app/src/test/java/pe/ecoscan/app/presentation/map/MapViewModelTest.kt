package pe.ecoscan.app.presentation.map

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.core.common.ErrorType
import pe.ecoscan.app.core.common.Resource
import pe.ecoscan.app.domain.model.CollectionPoint
import pe.ecoscan.app.domain.model.GeoPoint
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.usecase.FilterCollectionPointsUseCase
import pe.ecoscan.app.domain.usecase.GetCurrentLocationUseCase
import pe.ecoscan.app.domain.usecase.GetNearbyCollectionPointsUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeDispatcherProvider = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }

    private val getCurrentLocationUseCase = mockk<GetCurrentLocationUseCase>()
    private val getNearbyUseCase = mockk<GetNearbyCollectionPointsUseCase>()

    private val userLocation = GeoPoint(-12.1, -77.0)

    private fun point(id: String, distance: Int, vararg materials: WasteCategory) = CollectionPoint(
        id = id,
        name = id,
        location = GeoPoint(-12.1, -77.0),
        address = null,
        openingHours = null,
        acceptedMaterials = materials.toSet(),
        distanceMeters = distance
    )

    private val nearPlastic = point("a", 500, WasteCategory.PLASTIC)
    private val midGlass = point("b", 1_500, WasteCategory.GLASS)
    private val farPlastic = point("c", 4_000, WasteCategory.PLASTIC)
    private val points = listOf(nearPlastic, midGlass, farPlastic)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = MapViewModel(
        getCurrentLocationUseCase = getCurrentLocationUseCase,
        getNearbyCollectionPointsUseCase = getNearbyUseCase,
        filterCollectionPointsUseCase = FilterCollectionPointsUseCase(),
        dispatcherProvider = fakeDispatcherProvider
    )

    private fun givenLocationAndPoints(result: Resource<List<CollectionPoint>> = Resource.Success(points)) {
        coEvery { getCurrentLocationUseCase() } returns Result.success(userLocation)
        coEvery { getNearbyUseCase(userLocation, MapUiState.SEARCH_RADIUS_METERS) } returns result
    }

    @Test
    fun `el estado inicial no tiene permiso, ni carga, ni error`() {
        val state = createViewModel().uiState.value

        assertFalse(state.hasLocationPermission)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(MapUiState(), state)
    }

    @Test
    fun `al conceder el permiso obtiene la ubicacion y carga los puntos`() = runTest(testDispatcher) {
        givenLocationAndPoints()
        val viewModel = createViewModel()

        viewModel.onPermissionResult(true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.hasLocationPermission)
        assertEquals(userLocation, state.userLocation)
        assertEquals(points, state.allPoints)
        assertEquals(points, state.visiblePoints)
        assertFalse(state.isLoading)
        assertFalse(state.isOfflineData)
    }

    @Test
    fun `sin permiso no consulta la ubicacion ni la API`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.onPermissionResult(false)
        viewModel.loadNearbyPoints()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasLocationPermission)
        coVerify(exactly = 0) { getCurrentLocationUseCase() }
    }

    @Test
    fun `repetir el permiso concedido no vuelve a consultar`() = runTest(testDispatcher) {
        givenLocationAndPoints()
        val viewModel = createViewModel()

        viewModel.onPermissionResult(true)
        advanceUntilIdle()
        viewModel.onPermissionResult(true)
        advanceUntilIdle()

        coVerify(exactly = 1) { getCurrentLocationUseCase() }
    }

    @Test
    fun `si falla la ubicacion muestra el error y no busca puntos`() = runTest(testDispatcher) {
        coEvery { getCurrentLocationUseCase() } returns
                Result.failure(IllegalStateException("No se pudo obtener tu ubicación. Activa el GPS e inténtalo de nuevo."))
        val viewModel = createViewModel()

        viewModel.onPermissionResult(true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("No se pudo obtener tu ubicación. Activa el GPS e inténtalo de nuevo.", state.locationError)
        assertNull(state.userLocation)
        assertFalse(state.isLoading)
        coVerify(exactly = 0) { getNearbyUseCase(any(), any()) }
    }

    @Test
    fun `reintentar despues de un fallo de ubicacion carga los puntos`() = runTest(testDispatcher) {
        coEvery { getCurrentLocationUseCase() } returns Result.failure(IllegalStateException("sin GPS"))
        val viewModel = createViewModel()
        viewModel.onPermissionResult(true)
        advanceUntilIdle()

        givenLocationAndPoints()
        viewModel.loadNearbyPoints()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.locationError)
        assertEquals(points, state.visiblePoints)
    }

    @Test
    fun `un error de red con la ubicacion conocida muestra un mensaje y conserva el mapa`() = runTest(testDispatcher) {
        givenLocationAndPoints(Resource.Error("Sin conexión a internet.", ErrorType.NO_CONNECTION))
        val viewModel = createViewModel()

        viewModel.onPermissionResult(true)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Sin conexión a internet.", state.errorMessage)
        assertEquals(userLocation, state.userLocation)
        assertNull(state.locationError)
        assertFalse(state.isLoading)

        viewModel.clearError()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `los puntos guardados se marcan como datos sin conexion`() = runTest(testDispatcher) {
        givenLocationAndPoints(Resource.Success(points, isStale = true))
        val viewModel = createViewModel()

        viewModel.onPermissionResult(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isOfflineData)
    }

    @Test
    fun `filtrar por material deja solo los puntos que lo reciben`() = runTest(testDispatcher) {
        givenLocationAndPoints()
        val viewModel = createViewModel()
        viewModel.onPermissionResult(true)
        advanceUntilIdle()

        viewModel.onMaterialSelected(WasteCategory.PLASTIC)

        assertEquals(listOf(nearPlastic, farPlastic), viewModel.uiState.value.visiblePoints)
        assertEquals(points, viewModel.uiState.value.allPoints)

        viewModel.onMaterialSelected(null)

        assertEquals(points, viewModel.uiState.value.visiblePoints)
    }

    @Test
    fun `filtrar por distancia deja solo los puntos dentro del rango`() = runTest(testDispatcher) {
        givenLocationAndPoints()
        val viewModel = createViewModel()
        viewModel.onPermissionResult(true)
        advanceUntilIdle()

        viewModel.onMaxDistanceSelected(2_000)
        assertEquals(listOf(nearPlastic, midGlass), viewModel.uiState.value.visiblePoints)

        viewModel.onMaxDistanceSelected(1_000)
        assertEquals(listOf(nearPlastic), viewModel.uiState.value.visiblePoints)
    }

    @Test
    fun `cambiar un filtro no vuelve a llamar a la API`() = runTest(testDispatcher) {
        givenLocationAndPoints()
        val viewModel = createViewModel()
        viewModel.onPermissionResult(true)
        advanceUntilIdle()

        viewModel.onMaterialSelected(WasteCategory.GLASS)
        viewModel.onMaxDistanceSelected(1_000)
        advanceUntilIdle()

        coVerify(exactly = 1) { getNearbyUseCase(any(), any()) }
    }

    @Test
    fun `seleccionar un punto muestra su ficha y tocar el mapa la cierra`() = runTest(testDispatcher) {
        givenLocationAndPoints()
        val viewModel = createViewModel()
        viewModel.onPermissionResult(true)
        advanceUntilIdle()

        viewModel.onPointSelected(midGlass)
        assertEquals(midGlass, viewModel.uiState.value.selectedPoint)

        viewModel.onPointSelected(null)
        assertNull(viewModel.uiState.value.selectedPoint)
    }

    @Test
    fun `si un filtro oculta el punto seleccionado se cierra su ficha`() = runTest(testDispatcher) {
        givenLocationAndPoints()
        val viewModel = createViewModel()
        viewModel.onPermissionResult(true)
        advanceUntilIdle()
        viewModel.onPointSelected(midGlass)

        viewModel.onMaterialSelected(WasteCategory.PLASTIC)

        assertNull(viewModel.uiState.value.selectedPoint)
    }
}
