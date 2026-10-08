package pe.ecoscan.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pe.ecoscan.app.presentation.auth.LoginRoute
import pe.ecoscan.app.presentation.history.HistoryRoute
import pe.ecoscan.app.presentation.map.MapRoute
import pe.ecoscan.app.presentation.product.ProductDetailRoute
import pe.ecoscan.app.presentation.profile.ProfileRoute
import pe.ecoscan.app.presentation.scan.ScanRoute

// Ruta del detalle de producto: no es un destino de la barra inferior (no tiene ícono ni
// label propios), así que no se suma a la sealed class EcoScanDestination; se define aquí
// junto al resto de rutas sueltas como "login".
private const val PRODUCT_BARCODE_ARG = "barcode"
private const val PRODUCT_DETAIL_ROUTE = "product/{$PRODUCT_BARCODE_ARG}"

fun productDetailRoute(barcode: String) = "product/$barcode"

@Composable
fun EcoScanNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            LoginRoute(
                onLoginSuccess = {
                    navController.navigate(EcoScanDestination.Scan.route) {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable(EcoScanDestination.Scan.route) {
            ScanRoute(
                onNavigateToProduct = { barcode ->
                    navController.navigate(productDetailRoute(barcode))
                }
            )
        }
        composable(
            route = PRODUCT_DETAIL_ROUTE,
            arguments = listOf(navArgument(PRODUCT_BARCODE_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val barcode = backStackEntry.arguments?.getString(PRODUCT_BARCODE_ARG).orEmpty()
            ProductDetailRoute(
                barcode = barcode,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(EcoScanDestination.Map.route) { MapRoute() }
        composable(EcoScanDestination.History.route) { HistoryRoute() }
        composable(EcoScanDestination.Profile.route) {
            ProfileRoute(
                onSignedOut = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            )
        }
    }
}
