package pe.ecoscan.app.presentation.map

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import pe.ecoscan.app.R
import pe.ecoscan.app.core.designsystem.component.EcoScanButton
import pe.ecoscan.app.core.designsystem.component.EcoScanCard
import pe.ecoscan.app.core.designsystem.component.EcoScanEmptyState
import pe.ecoscan.app.core.designsystem.component.EcoScanLoading
import pe.ecoscan.app.core.designsystem.component.EcoScanTopBar
import pe.ecoscan.app.core.designsystem.theme.EcoScanTheme
import pe.ecoscan.app.domain.model.CollectionPoint
import pe.ecoscan.app.domain.model.GeoPoint
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.presentation.history.toDisplayName

private const val DEFAULT_ZOOM = 14f
private const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"

// Punto de entrada con Hilt. Aquí viven el permiso de ubicación y la apertura de Google Maps;
// MapScreen queda sin dependencias de Android para poder previsualizarla.
// hiltViewModel() de androidx.hilt.navigation.compose está deprecado a favor de
// androidx.hilt.lifecycle.viewmodel.compose, pero ese artefacto sigue en alpha/beta;
// nos quedamos en la versión estable hasta que haya un release estable.
@Suppress("DEPRECATION")
@Composable
fun MapRoute(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        viewModel.onPermissionResult(result.values.any { it })
    }
    val requestPermission = {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    // Al abrir la pestaña se pide el permiso si aún no está concedido.
    LaunchedEffect(Unit) {
        if (!context.hasLocationPermission()) requestPermission()
    }

    // Al volver a la pantalla (por ejemplo, desde Ajustes) se vuelve a comprobar el permiso.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onPermissionResult(context.hasLocationPermission())
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    MapScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onRequestPermission = { requestPermission() },
        onOpenSettings = { context.openAppSettings() },
        onRetry = viewModel::loadNearbyPoints,
        onMaterialSelected = viewModel::onMaterialSelected,
        onMaxDistanceSelected = viewModel::onMaxDistanceSelected,
        onPointSelected = viewModel::onPointSelected,
        onDirectionsClick = { point -> context.openDirections(point) },
        modifier = modifier
    )
}

@Composable
fun MapScreen(
    uiState: MapUiState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onRequestPermission: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onRetry: () -> Unit = {},
    onMaterialSelected: (WasteCategory?) -> Unit = {},
    onMaxDistanceSelected: (Int) -> Unit = {},
    onPointSelected: (CollectionPoint?) -> Unit = {},
    onDirectionsClick: (CollectionPoint) -> Unit = {}
) {
    Scaffold(
        modifier = modifier,
        topBar = { EcoScanTopBar(title = stringResource(R.string.map_title)) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        val location = uiState.userLocation
        val locationError = uiState.locationError

        when {
            !uiState.hasLocationPermission -> PermissionRequired(
                onRequestPermission = onRequestPermission,
                onOpenSettings = onOpenSettings,
                modifier = contentModifier
            )

            location == null && locationError != null -> EcoScanEmptyState(
                message = locationError,
                actionLabel = stringResource(R.string.map_retry),
                onAction = onRetry,
                modifier = contentModifier
            )

            location == null -> EcoScanLoading(modifier = contentModifier)

            else -> MapContent(
                uiState = uiState,
                userLocation = location,
                onMaterialSelected = onMaterialSelected,
                onMaxDistanceSelected = onMaxDistanceSelected,
                onPointSelected = onPointSelected,
                onRefresh = onRetry,
                onDirectionsClick = onDirectionsClick,
                modifier = contentModifier
            )
        }
    }
}

@Composable
private fun PermissionRequired(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EcoScanEmptyState(
            message = stringResource(R.string.map_permission_message),
            actionLabel = stringResource(R.string.map_permission_action),
            onAction = onRequestPermission
        )
        // Si el usuario negó el permiso dos veces, Android ya no muestra el diálogo:
        // la única salida es activarlo desde los ajustes de la app.
        TextButton(onClick = onOpenSettings) {
            Text(stringResource(R.string.map_open_settings))
        }
    }
}

@Composable
private fun MapContent(
    uiState: MapUiState,
    userLocation: GeoPoint,
    onMaterialSelected: (WasteCategory?) -> Unit,
    onMaxDistanceSelected: (Int) -> Unit,
    onPointSelected: (CollectionPoint?) -> Unit,
    onRefresh: () -> Unit,
    onDirectionsClick: (CollectionPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState()
    val scope = rememberCoroutineScope()

    // Centra el mapa en el usuario cada vez que se obtiene una ubicación nueva.
    LaunchedEffect(userLocation) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(
            LatLng(userLocation.latitude, userLocation.longitude),
            DEFAULT_ZOOM
        )
    }

    // Al tocar un punto, la cámara se desplaza hasta él.
    LaunchedEffect(uiState.selectedPoint?.id) {
        uiState.selectedPoint?.let { point ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLng(LatLng(point.location.latitude, point.location.longitude))
            )
        }
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = uiState.hasLocationPermission),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
            onMapClick = { onPointSelected(null) }
        ) {
            uiState.visiblePoints.forEach { point ->
                key(point.id) {
                    val isSelected = point.id == uiState.selectedPoint?.id
                    Marker(
                        state = rememberUpdatedMarkerState(
                            position = LatLng(point.location.latitude, point.location.longitude)
                        ),
                        title = point.name,
                        icon = BitmapDescriptorFactory.defaultMarker(
                            if (isSelected) BitmapDescriptorFactory.HUE_AZURE else BitmapDescriptorFactory.HUE_GREEN
                        ),
                        onClick = {
                            onPointSelected(point)
                            true
                        }
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MapFilters(
                selectedMaterial = uiState.selectedMaterial,
                maxDistanceMeters = uiState.maxDistanceMeters,
                onMaterialSelected = onMaterialSelected,
                onMaxDistanceSelected = onMaxDistanceSelected
            )
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (uiState.isOfflineData) {
                StatusBanner(text = stringResource(R.string.map_offline_data))
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val selected = uiState.selectedPoint
            if (selected == null) {
                SmallFloatingActionButton(
                    onClick = {
                        scope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(userLocation.latitude, userLocation.longitude),
                                    DEFAULT_ZOOM
                                )
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = stringResource(R.string.map_my_location)
                    )
                }
                FloatingActionButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = stringResource(R.string.map_refresh)
                    )
                }
                if (uiState.visiblePoints.isEmpty() && !uiState.isLoading) {
                    StatusBanner(text = stringResource(R.string.map_no_points))
                }
            } else {
                PointDetailCard(
                    point = selected,
                    onClose = { onPointSelected(null) },
                    onDirectionsClick = { onDirectionsClick(selected) }
                )
            }
        }
    }
}

@Composable
private fun MapFilters(
    selectedMaterial: WasteCategory?,
    maxDistanceMeters: Int,
    onMaterialSelected: (WasteCategory?) -> Unit,
    onMaxDistanceSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedMaterial == null,
                    onClick = { onMaterialSelected(null) },
                    label = { Text(stringResource(R.string.map_filter_all_materials)) }
                )
                WasteCategory.entries.forEach { category ->
                    FilterChip(
                        selected = selectedMaterial == category,
                        onClick = { onMaterialSelected(category) },
                        label = { Text(category.toDisplayName()) }
                    )
                }
            }
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MapUiState.DISTANCE_OPTIONS_METERS.forEach { meters ->
                    FilterChip(
                        selected = maxDistanceMeters == meters,
                        onClick = { onMaxDistanceSelected(meters) },
                        label = { Text(stringResource(R.string.map_filter_distance_format, meters / 1000)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBanner(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shadowElevation = 2.dp
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// Ficha del punto: nombre, distancia, dirección, horario, materiales y "Cómo llegar".
@Composable
private fun PointDetailCard(
    point: CollectionPoint,
    onClose: () -> Unit,
    onDirectionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EcoScanCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = point.name ?: stringResource(R.string.map_point_unnamed),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.map_point_distance_format, formatDistance(point.distanceMeters)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.map_close_details)
                )
            }
        }
        DetailLine(
            label = stringResource(R.string.map_point_address_label),
            value = point.address ?: stringResource(R.string.map_point_address_unknown)
        )
        DetailLine(
            label = stringResource(R.string.map_point_hours_label),
            value = formatOpeningHours(point.openingHours) ?: stringResource(R.string.map_point_hours_unknown)
        )
        DetailLine(
            label = stringResource(R.string.map_point_materials_label),
            value = if (point.acceptedMaterials.isEmpty()) {
                stringResource(R.string.map_point_materials_unknown)
            } else {
                point.acceptedMaterials.map { it.toDisplayName() }.joinToString(", ")
            }
        )
        EcoScanButton(
            text = stringResource(R.string.map_directions),
            onClick = onDirectionsClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column(modifier = Modifier.padding(top = 6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

private fun Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, R.string.map_settings_unavailable, Toast.LENGTH_SHORT).show()
    }
}

// "Cómo llegar": abre la navegación de Google Maps hacia el punto. Si Google Maps no está
// instalado, abre cualquier app de mapas con la ubicación.
private fun Context.openDirections(point: CollectionPoint) {
    val lat = point.location.latitude
    val lon = point.location.longitude
    val navigation = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$lat,$lon"))
        .setPackage(GOOGLE_MAPS_PACKAGE)
    val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lon?q=$lat,$lon"))
    try {
        startActivity(navigation)
    } catch (e: ActivityNotFoundException) {
        try {
            startActivity(fallback)
        } catch (e2: ActivityNotFoundException) {
            Toast.makeText(this, R.string.map_no_maps_app, Toast.LENGTH_SHORT).show()
        }
    }
}

@PreviewLightDark
@Composable
private fun MapPermissionPreview() {
    EcoScanTheme {
        MapScreen(uiState = MapUiState(hasLocationPermission = false))
    }
}

@PreviewLightDark
@Composable
private fun MapLocationErrorPreview() {
    EcoScanTheme {
        MapScreen(
            uiState = MapUiState(
                hasLocationPermission = true,
                locationError = "No se pudo obtener tu ubicación. Activa el GPS e inténtalo de nuevo."
            )
        )
    }
}

@PreviewLightDark
@Composable
private fun PointDetailCardPreview() {
    EcoScanTheme {
        PointDetailCard(
            point = CollectionPoint(
                id = "node/1",
                name = "Punto de acopio Miraflores",
                location = GeoPoint(-12.12, -77.03),
                address = "Av. Larco 345, Miraflores",
                openingHours = "Mo-Fr 09:00-18:00; Sa 09:00-13:00",
                acceptedMaterials = setOf(WasteCategory.PLASTIC, WasteCategory.GLASS),
                distanceMeters = 850
            ),
            onClose = {},
            onDirectionsClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}