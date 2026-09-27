package pe.ecoscan.app.presentation.scan

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.ecoscan.app.core.designsystem.component.EcoScanEmptyState
import pe.ecoscan.app.core.designsystem.component.EcoScanTopBar
import pe.ecoscan.app.core.designsystem.theme.EcoScanTheme

// Punto de entrada con Hilt. Se llama solo aquí para que ScanScreen sea previsualizable
// y testeable sin necesidad de un contenedor de inyección.
@Composable
fun ScanRoute(
    modifier: Modifier = Modifier,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScanScreen(uiState = uiState, modifier = modifier)
}

@Composable
fun ScanScreen(
    uiState: ScanUiState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { EcoScanTopBar(title = "Escanear") }
    ) { paddingValues ->
        EcoScanEmptyState(
            message = uiState.errorMessage ?: "Disponible en el Sprint 3",
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@PreviewLightDark
@Composable
private fun ScanScreenPreview() {
    EcoScanTheme {
        ScanScreen(uiState = ScanUiState())
    }
}
