package pe.ecoscan.app.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import pe.ecoscan.app.core.designsystem.component.EcoScanButton
import pe.ecoscan.app.core.designsystem.component.EcoScanButtonVariant
import pe.ecoscan.app.core.designsystem.component.EcoScanCard
import pe.ecoscan.app.core.designsystem.component.EcoScanEmptyState
import pe.ecoscan.app.core.designsystem.component.EcoScanLoading
import pe.ecoscan.app.core.designsystem.component.EcoScanTopBar
import pe.ecoscan.app.core.designsystem.theme.EcoScanTheme

private data class ColorSwatch(val name: String, val color: Color)

// Pantalla de referencia que reúne paleta, tipografía y componentes del sistema de diseño.
@Composable
fun DesignSystemCatalog(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = { EcoScanTopBar(title = "Sistema de diseño EcoScan") }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { CatalogSection(title = "Paleta de colores") { ColorPaletteSample() } }
            item { CatalogSection(title = "Tipografía") { TypographySample() } }
            item { CatalogSection(title = "Botones") { ButtonsSample() } }
            item { CatalogSection(title = "Tarjeta") { CardSample() } }
            item { CatalogSection(title = "Estado vacío") { EmptyStateSample() } }
            item { CatalogSection(title = "Carga") { LoadingSample() } }
        }
    }
}

@Composable
private fun CatalogSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun ColorPaletteSample() {
    val swatches = listOf(
        ColorSwatch("primary", MaterialTheme.colorScheme.primary),
        ColorSwatch("secondary", MaterialTheme.colorScheme.secondary),
        ColorSwatch("tertiary", MaterialTheme.colorScheme.tertiary),
        ColorSwatch("error", MaterialTheme.colorScheme.error),
        ColorSwatch("surfaceVariant", MaterialTheme.colorScheme.surfaceVariant)
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        swatches.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { swatch ->
                    Column {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(swatch.color, RoundedCornerShape(12.dp))
                        )
                        Text(text = swatch.name, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun TypographySample() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = "Headline large", style = MaterialTheme.typography.headlineLarge)
        Text(text = "Title medium", style = MaterialTheme.typography.titleMedium)
        Text(text = "Body large", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Label small", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ButtonsSample() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EcoScanButton(text = "Primario", onClick = {}, variant = EcoScanButtonVariant.PRIMARY)
        EcoScanButton(text = "Tonal", onClick = {}, variant = EcoScanButtonVariant.TONAL)
        EcoScanButton(text = "Texto", onClick = {}, variant = EcoScanButtonVariant.TEXT)
        EcoScanButton(text = "Cargando", onClick = {}, isLoading = true)
        EcoScanButton(text = "Deshabilitado", onClick = {}, enabled = false)
    }
}

@Composable
private fun CardSample() {
    EcoScanCard {
        Text(text = "Punto de acopio Miraflores", style = MaterialTheme.typography.titleMedium)
        Text(text = "Recicla vidrio, papel y plástico", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EmptyStateSample() {
    Box(modifier = Modifier.height(220.dp)) {
        EcoScanEmptyState(
            message = "Aún no hay puntos de acopio cerca de ti",
            actionLabel = "Reintentar",
            onAction = {}
        )
    }
}

@Composable
private fun LoadingSample() {
    Box(modifier = Modifier.height(120.dp)) {
        EcoScanLoading()
    }
}

@PreviewLightDark
@Composable
private fun DesignSystemCatalogPreview() {
    EcoScanTheme {
        DesignSystemCatalog()
    }
}
