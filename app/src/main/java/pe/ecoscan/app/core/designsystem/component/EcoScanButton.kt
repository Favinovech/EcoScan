package pe.ecoscan.app.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import pe.ecoscan.app.core.designsystem.theme.EcoScanTheme

// Variante visual de EcoScanButton.
enum class EcoScanButtonVariant {
    PRIMARY,
    TONAL,
    TEXT
}

@Composable
fun EcoScanButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: EcoScanButtonVariant = EcoScanButtonVariant.PRIMARY,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val isEnabled = enabled && !isLoading
    val label: @Composable () -> Unit = {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = LocalContentColorForVariant(variant)
            )
        } else {
            Text(text = text)
        }
    }

    when (variant) {
        EcoScanButtonVariant.PRIMARY -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = isEnabled,
            content = { label() }
        )

        EcoScanButtonVariant.TONAL -> FilledTonalButton(
            onClick = onClick,
            modifier = modifier,
            enabled = isEnabled,
            content = { label() }
        )

        EcoScanButtonVariant.TEXT -> TextButton(
            onClick = onClick,
            modifier = modifier,
            enabled = isEnabled,
            content = { label() }
        )
    }
}

@Composable
private fun LocalContentColorForVariant(variant: EcoScanButtonVariant) = when (variant) {
    EcoScanButtonVariant.PRIMARY -> MaterialTheme.colorScheme.onPrimary
    EcoScanButtonVariant.TONAL -> MaterialTheme.colorScheme.onSecondaryContainer
    EcoScanButtonVariant.TEXT -> MaterialTheme.colorScheme.primary
}

@PreviewLightDark
@Composable
private fun EcoScanButtonPreview() {
    EcoScanTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            EcoScanButton(text = "Primario", onClick = {}, variant = EcoScanButtonVariant.PRIMARY)
            EcoScanButton(text = "Tonal", onClick = {}, variant = EcoScanButtonVariant.TONAL)
            EcoScanButton(text = "Texto", onClick = {}, variant = EcoScanButtonVariant.TEXT)
            EcoScanButton(text = "Cargando", onClick = {}, isLoading = true)
            EcoScanButton(text = "Deshabilitado", onClick = {}, enabled = false)
        }
    }
}
