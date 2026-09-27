package pe.ecoscan.app.presentation.map

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.ecoscan.app.R
import pe.ecoscan.app.core.designsystem.component.EcoScanEmptyState
import pe.ecoscan.app.core.designsystem.component.EcoScanTopBar
import pe.ecoscan.app.core.designsystem.theme.EcoScanTheme

// Punto de entrada con Hilt. Se llama solo aquí para que MapScreen sea previsualizable
// y testeable sin necesidad de un contenedor de inyección.
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
    MapScreen(uiState = uiState, modifier = modifier)
}

@Composable
fun MapScreen(
    uiState: MapUiState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { EcoScanTopBar(title = stringResource(R.string.map_title)) }
    ) { paddingValues ->
        EcoScanEmptyState(
            message = uiState.errorMessage ?: stringResource(R.string.placeholder_available_next_sprint),
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@PreviewLightDark
@Composable
private fun MapScreenPreview() {
    EcoScanTheme {
        MapScreen(uiState = MapUiState())
    }
}
