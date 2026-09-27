package pe.ecoscan.app.presentation.navigation

import androidx.annotation.StringRes
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
import pe.ecoscan.app.R

// Clase sellada con los destinos de la barra inferior: ruta, etiqueta y sus dos iconos.
sealed class EcoScanDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Scan : EcoScanDestination(
        route = "scan",
        labelRes = R.string.nav_label_scan,
        selectedIcon = Icons.Filled.CameraAlt,
        unselectedIcon = Icons.Outlined.CameraAlt
    )

    data object Map : EcoScanDestination(
        route = "map",
        labelRes = R.string.nav_label_map,
        selectedIcon = Icons.Filled.LocationOn,
        unselectedIcon = Icons.Outlined.LocationOn
    )

    data object History : EcoScanDestination(
        route = "history",
        labelRes = R.string.nav_label_history,
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    )

    data object Profile : EcoScanDestination(
        route = "profile",
        labelRes = R.string.nav_label_profile,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    companion object {
        val bottomBarDestinations = listOf(Scan, Map, History, Profile)
    }
}
