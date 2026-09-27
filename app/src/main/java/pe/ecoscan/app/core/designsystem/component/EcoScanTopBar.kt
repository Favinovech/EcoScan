package pe.ecoscan.app.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import pe.ecoscan.app.R
import pe.ecoscan.app.core.designsystem.theme.EcoScanTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoScanTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(text = title) },
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.content_description_back)
                    )
                }
            }
        },
        actions = { actions() }
    )
}

@PreviewLightDark
@Composable
private fun EcoScanTopBarPreview() {
    EcoScanTheme {
        EcoScanTopBar(title = "Puntos de acopio", onNavigateBack = {})
    }
}
