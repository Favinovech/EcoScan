package pe.ecoscan.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pe.ecoscan.app.presentation.history.HistoryRoute
import pe.ecoscan.app.presentation.map.MapRoute
import pe.ecoscan.app.presentation.profile.ProfileRoute
import pe.ecoscan.app.presentation.scan.ScanRoute

@Composable
fun EcoScanNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = EcoScanDestination.Scan.route,
        modifier = modifier
    ) {
        composable(EcoScanDestination.Scan.route) { ScanRoute() }
        composable(EcoScanDestination.Map.route) { MapRoute() }
        composable(EcoScanDestination.History.route) { HistoryRoute() }
        composable(EcoScanDestination.Profile.route) { ProfileRoute() }
    }
}
