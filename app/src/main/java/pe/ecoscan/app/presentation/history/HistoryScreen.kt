package pe.ecoscan.app.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.ecoscan.app.R
import pe.ecoscan.app.core.common.toFormattedDate
import pe.ecoscan.app.core.designsystem.component.EcoScanCard
import pe.ecoscan.app.core.designsystem.component.EcoScanEmptyState
import pe.ecoscan.app.core.designsystem.component.EcoScanLoading
import pe.ecoscan.app.core.designsystem.component.EcoScanTopBar
import pe.ecoscan.app.core.designsystem.theme.EcoScanTheme
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteRecord

// Punto de entrada con Hilt. Se llama solo aquí para que HistoryScreen sea previsualizable
// y testeable sin necesidad de un contenedor de inyección.
// hiltViewModel() de androidx.hilt.navigation.compose está deprecado a favor de
// androidx.hilt.lifecycle.viewmodel.compose, pero ese artefacto sigue en alpha/beta;
// nos quedamos en la versión estable hasta que haya un release estable.
@Suppress("DEPRECATION")
@Composable
fun HistoryRoute(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryScreen(uiState = uiState, modifier = modifier)
}

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { EcoScanTopBar(title = stringResource(R.string.history_title)) }
    ) { paddingValues ->
        when {
            uiState.isLoading -> EcoScanLoading(modifier = Modifier.padding(paddingValues))

            uiState.records.isEmpty() -> EcoScanEmptyState(
                message = uiState.errorMessage ?: stringResource(R.string.history_empty_message),
                modifier = Modifier.padding(paddingValues)
            )

            else -> HistoryContent(
                uiState = uiState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@Composable
private fun HistoryContent(uiState: HistoryUiState, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            EcoScanCard {
                Text(
                    text = stringResource(R.string.history_points_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.history_total_points_format, uiState.totalPoints),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        items(items = uiState.records, key = { it.id }) { record ->
            EcoScanCard {
                Text(text = record.category.toDisplayName(), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.history_record_format, record.weightKg, record.points),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = record.recordedAt.toFormattedDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WasteCategory.toDisplayName(): String = stringResource(
    when (this) {
        WasteCategory.PLASTIC -> R.string.waste_category_plastic
        WasteCategory.GLASS -> R.string.waste_category_glass
        WasteCategory.PAPER -> R.string.waste_category_paper
        WasteCategory.METAL -> R.string.waste_category_metal
        WasteCategory.ORGANIC -> R.string.waste_category_organic
        WasteCategory.HAZARDOUS -> R.string.waste_category_hazardous
    }
)

@PreviewLightDark
@Composable
private fun HistoryScreenLoadingPreview() {
    EcoScanTheme {
        HistoryScreen(uiState = HistoryUiState(isLoading = true))
    }
}

@PreviewLightDark
@Composable
private fun HistoryScreenContentPreview() {
    EcoScanTheme {
        HistoryScreen(
            uiState = HistoryUiState(
                isLoading = false,
                records = listOf(
                    WasteRecord("1", WasteCategory.PLASTIC, weightKg = 2.5, points = 25, recordedAt = System.currentTimeMillis()),
                    WasteRecord("2", WasteCategory.GLASS, weightKg = 5.0, points = 30, recordedAt = System.currentTimeMillis())
                ),
                totalPoints = 55
            )
        )
    }
}

@PreviewLightDark
@Composable
private fun HistoryScreenEmptyPreview() {
    EcoScanTheme {
        HistoryScreen(uiState = HistoryUiState(isLoading = false))
    }
}
