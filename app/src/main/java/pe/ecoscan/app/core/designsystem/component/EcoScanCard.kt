package pe.ecoscan.app.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import pe.ecoscan.app.core.designsystem.theme.EcoScanTheme

@Composable
fun EcoScanCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@PreviewLightDark
@Composable
private fun EcoScanCardPreview() {
    EcoScanTheme {
        EcoScanCard(modifier = Modifier.padding(16.dp)) {
            Text(text = "Punto de acopio Miraflores")
            Text(text = "Recicla vidrio, papel y plástico")
        }
    }
}
