package pe.ecoscan.app.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Prueba de UI de la barra inferior y su integración con la navegación. Usa pantallas de
// prueba en lugar de las reales (ScanRoute, MapRoute, ...) porque esas dependen de
// hiltViewModel() y montar Hilt de verdad en un instrumentado requiere infraestructura
// (HiltTestRunner, HiltTestApplication) que no forma parte de esta parte del sprint.
@RunWith(AndroidJUnit4::class)
class EcoScanBottomBarNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun label(destination: EcoScanDestination): String = context.getString(destination.labelRes)

    @Test
    fun laBarraInferiorMuestraLosCuatroDestinosYNavegaAlTocarUno() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            val currentBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = currentBackStackEntry?.destination?.route

            Scaffold(
                bottomBar = {
                    EcoScanBottomBar(
                        currentRoute = currentRoute,
                        onDestinationSelected = { destination ->
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = EcoScanDestination.Scan.route,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    EcoScanDestination.bottomBarDestinations.forEach { destination ->
                        composable(destination.route) {
                            Text(text = "Pantalla ${destination.route}")
                        }
                    }
                }
            }
        }

        EcoScanDestination.bottomBarDestinations.forEach { destination ->
            composeTestRule.onNodeWithText(label(destination)).assertExists()
        }

        composeTestRule.onNodeWithText("Pantalla ${EcoScanDestination.Scan.route}").assertExists()

        composeTestRule.onNodeWithText(label(EcoScanDestination.Map)).performClick()

        composeTestRule.onNodeWithText("Pantalla ${EcoScanDestination.Map.route}").assertExists()
    }
}
