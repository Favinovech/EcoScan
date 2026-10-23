package pe.ecoscan.app.presentation.history

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    HistoryScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onCategorySelected = viewModel::onCategorySelected,
        onDatePresetSelected = viewModel::onDatePresetSelected,
        onCustomDateRangeSelected = viewModel::onCustomDateRangeSelected,
        onClearFilters = viewModel::clearFilters,
        onEditRequested = viewModel::onEditRequested,
        onEditDismissed = viewModel::onEditDismissed,
        onEditConfirmed = viewModel::onEditConfirmed,
        onDeleteRequested = viewModel::onDeleteRequested,
        onDeleteDismissed = viewModel::onDeleteDismissed,
        onDeleteConfirmed = viewModel::onDeleteConfirmed,
        modifier = modifier
    )
}

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onCategorySelected: (WasteCategory?) -> Unit = {},
    onDatePresetSelected: (DatePreset) -> Unit = {},
    onCustomDateRangeSelected: (Long, Long?) -> Unit = { _, _ -> },
    onClearFilters: () -> Unit = {},
    onEditRequested: (WasteRecord) -> Unit = {},
    onEditDismissed: () -> Unit = {},
    onEditConfirmed: (WasteCategory, String) -> Unit = { _, _ -> },
    onDeleteRequested: (WasteRecord) -> Unit = {},
    onDeleteDismissed: () -> Unit = {},
    onDeleteConfirmed: () -> Unit = {}
) {
    var showDateRangeDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = { EcoScanTopBar(title = stringResource(R.string.history_title)) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading -> EcoScanLoading(modifier = Modifier.padding(paddingValues))

            // Sin ningún registro (distinto de "el filtro no encontró nada").
            uiState.totalRecords == 0 -> EcoScanEmptyState(
                message = uiState.errorMessage ?: stringResource(R.string.history_empty_message),
                modifier = Modifier.padding(paddingValues)
            )

            else -> HistoryContent(
                uiState = uiState,
                onCategorySelected = onCategorySelected,
                onDatePresetSelected = onDatePresetSelected,
                onCustomRangeClick = { showDateRangeDialog = true },
                onClearFilters = onClearFilters,
                onEditRequested = onEditRequested,
                onDeleteRequested = onDeleteRequested,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }

    if (showDateRangeDialog) {
        DateRangeDialog(
            onConfirm = { start, end ->
                showDateRangeDialog = false
                onCustomDateRangeSelected(start, end)
            },
            onDismiss = { showDateRangeDialog = false }
        )
    }
    uiState.editingRecord?.let { record ->
        EditRecordDialog(
            record = record,
            error = uiState.editError,
            onConfirm = onEditConfirmed,
            onDismiss = onEditDismissed
        )
    }
    uiState.recordPendingDeletion?.let { record ->
        DeleteRecordDialog(
            record = record,
            onConfirm = onDeleteConfirmed,
            onDismiss = onDeleteDismissed
        )
    }
}

@Composable
private fun HistoryContent(
    uiState: HistoryUiState,
    onCategorySelected: (WasteCategory?) -> Unit,
    onDatePresetSelected: (DatePreset) -> Unit,
    onCustomRangeClick: () -> Unit,
    onClearFilters: () -> Unit,
    onEditRequested: (WasteRecord) -> Unit,
    onDeleteRequested: (WasteRecord) -> Unit,
    modifier: Modifier = Modifier
) {
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

        item {
            HistoryFilters(
                uiState = uiState,
                onCategorySelected = onCategorySelected,
                onDatePresetSelected = onDatePresetSelected,
                onCustomRangeClick = onCustomRangeClick,
                onClearFilters = onClearFilters
            )
        }

        if (uiState.records.isEmpty()) {
            item {
                EcoScanEmptyState(
                    message = stringResource(R.string.history_no_results),
                    actionLabel = stringResource(R.string.history_clear_filters),
                    onAction = onClearFilters
                )
            }
        } else {
            items(items = uiState.records, key = { it.id }) { record ->
                WasteRecordCard(
                    record = record,
                    onEdit = { onEditRequested(record) },
                    onDelete = { onDeleteRequested(record) }
                )
            }
        }
    }
}

@Composable
private fun HistoryFilters(
    uiState: HistoryUiState,
    onCategorySelected: (WasteCategory?) -> Unit,
    onDatePresetSelected: (DatePreset) -> Unit,
    onCustomRangeClick: () -> Unit,
    onClearFilters: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.history_filter_category_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text(stringResource(R.string.history_filter_all_categories)) }
            )
            WasteCategory.entries.forEach { category ->
                FilterChip(
                    selected = uiState.selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.toDisplayName()) }
                )
            }
        }

        Text(
            text = stringResource(R.string.history_filter_date_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.datePreset == DatePreset.ALL,
                onClick = { onDatePresetSelected(DatePreset.ALL) },
                label = { Text(stringResource(R.string.history_date_all)) }
            )
            FilterChip(
                selected = uiState.datePreset == DatePreset.LAST_7_DAYS,
                onClick = { onDatePresetSelected(DatePreset.LAST_7_DAYS) },
                label = { Text(stringResource(R.string.history_date_7_days)) }
            )
            FilterChip(
                selected = uiState.datePreset == DatePreset.LAST_30_DAYS,
                onClick = { onDatePresetSelected(DatePreset.LAST_30_DAYS) },
                label = { Text(stringResource(R.string.history_date_30_days)) }
            )
            FilterChip(
                selected = uiState.datePreset == DatePreset.CUSTOM,
                onClick = onCustomRangeClick,
                label = {
                    val range = uiState.customRange
                    Text(
                        if (uiState.datePreset == DatePreset.CUSTOM && range != null) {
                            "${range.fromMillis.toFormattedDate()} – ${range.toMillis.toFormattedDate()}"
                        } else {
                            stringResource(R.string.history_date_custom)
                        }
                    )
                }
            )
        }

        if (uiState.hasActiveFilters) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(
                        R.string.history_results_format,
                        uiState.records.size,
                        uiState.totalRecords,
                        String.format("%.1f", uiState.filteredWeightKg)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onClearFilters) {
                    Text(stringResource(R.string.history_clear_filters))
                }
            }
        }
    }
}

@Composable
private fun WasteRecordCard(
    record: WasteRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    EcoScanCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
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
                if (!record.isSynced) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CloudOff,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.history_pending_sync),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.history_edit_action)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.history_delete_action),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
internal fun WasteCategory.toDisplayName(): String = stringResource(
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
        val records = listOf(
            WasteRecord("1", WasteCategory.PLASTIC, weightKg = 2.5, points = 37, recordedAt = System.currentTimeMillis(), isSynced = true),
            WasteRecord("2", WasteCategory.GLASS, weightKg = 5.0, points = 100, recordedAt = System.currentTimeMillis())
        )
        HistoryScreen(
            uiState = HistoryUiState(
                isLoading = false,
                records = records,
                totalRecords = 2,
                totalPoints = 137
            )
        )
    }
}

@PreviewLightDark
@Composable
private fun HistoryScreenNoResultsPreview() {
    EcoScanTheme {
        HistoryScreen(
            uiState = HistoryUiState(
                isLoading = false,
                totalRecords = 4,
                totalPoints = 137,
                selectedCategory = WasteCategory.METAL
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