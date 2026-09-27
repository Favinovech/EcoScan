package pe.ecoscan.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

// Clase sellada con los destinos de la barra inferior: ruta, etiqueta y sus dos iconos.
sealed class EcoScanDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Scan : EcoScanDestination(
        route = "scan",
        label = "Escanear",
        selectedIcon = Icons.Filled.CameraAlt,
        unselectedIcon = Icons.Outlined.CameraAlt
    )

    data object Map : EcoScanDestination(
        route = "map",
        label = "Mapa",
        selectedIcon = Icons.Filled.LocationOn,
        unselectedIcon = Icons.Outlined.LocationOn
    )

    data object History : EcoScanDestination(
        route = "history",
        label = "Historial",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    )

    data object Profile : EcoScanDestination(
        route = "profile",
        label = "Perfil",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    companion object {
        val bottomBarDestinations = listOf(Scan, Map, History, Profile)
    }
}
