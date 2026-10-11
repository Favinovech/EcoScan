package pe.ecoscan.app.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import pe.ecoscan.app.R
import pe.ecoscan.app.core.common.toFormattedDate
import pe.ecoscan.app.core.designsystem.theme.EcoScanTheme
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteRecord
import java.math.BigDecimal

// Edita categoría y peso. Los puntos se recalculan al guardar (UpdateWasteRecordUseCase).
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditRecordDialog(
    record: WasteRecord,
    error: String?,
    onConfirm: (category: WasteCategory, weightText: String) -> Unit,
    onDismiss: () -> Unit
) {
    var category by rememberSaveable(record.id) { mutableStateOf(record.category) }
    var weightText by rememberSaveable(record.id) {
        mutableStateOf(BigDecimal.valueOf(record.weightKg).stripTrailingZeros().toPlainString())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.history_edit_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.history_edit_category_label),
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WasteCategory.entries.forEach { option ->
                        FilterChip(
                            selected = option == category,
                            onClick = { category = option },
                            label = { Text(option.toDisplayName()) }
                        )
                    }
                }
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text(stringResource(R.string.history_edit_weight_label)) },
                    singleLine = true,
                    isError = error != null,
                    supportingText = if (error != null) {
                        { Text(error) }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(category, weightText) }) {
                Text(stringResource(R.string.history_edit_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@Composable
fun DeleteRecordDialog(
    record: WasteRecord,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.history_delete_title)) },
        text = {
            Text(
                stringResource(
                    R.string.history_delete_message_format,
                    record.category.toDisplayName(),
                    record.recordedAt.toFormattedDate(),
                    BigDecimal.valueOf(record.weightKg).stripTrailingZeros().toPlainString()
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.history_delete_confirm),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

// Selector de rango de fechas de Material 3. Devuelve la medianoche UTC de cada día elegido
// (el ViewModel lo convierte a la zona horaria del dispositivo).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeDialog(
    onConfirm: (startUtcMillis: Long, endUtcMillis: Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDateRangePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = state.selectedStartDateMillis != null,
                onClick = {
                    state.selectedStartDateMillis?.let { start ->
                        onConfirm(start, state.selectedEndDateMillis)
                    }
                }
            ) {
                Text(stringResource(R.string.history_date_apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    ) {
        DateRangePicker(state = state, modifier = Modifier.height(500.dp))
    }
}

@PreviewLightDark
@Composable
private fun DeleteRecordDialogPreview() {
    EcoScanTheme {
        DeleteRecordDialog(
            record = WasteRecord("1", WasteCategory.PLASTIC, 2.5, 37, System.currentTimeMillis()),
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun EditRecordDialogPreview() {
    EcoScanTheme {
        EditRecordDialog(
            record = WasteRecord("1", WasteCategory.GLASS, 5.0, 100, System.currentTimeMillis()),
            error = null,
            onConfirm = { _, _ -> },
            onDismiss = {}
        )
    }
}