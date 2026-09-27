package pe.ecoscan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import pe.ecoscan.app.core.designsystem.DesignSystemCatalog
import pe.ecoscan.app.core.designsystem.theme.EcoScanTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcoScanTheme {
                // Se reemplaza por la navegación real en la Parte 5 del sprint.
                DesignSystemCatalog()
            }
        }
    }
}
