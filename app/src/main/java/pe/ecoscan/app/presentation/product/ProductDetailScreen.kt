package pe.ecoscan.app.presentation.product

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import pe.ecoscan.app.core.common.ErrorType
import pe.ecoscan.app.core.designsystem.component.EcoScanCard
import pe.ecoscan.app.core.designsystem.component.EcoScanEmptyState
import pe.ecoscan.app.core.designsystem.component.EcoScanLoading
import pe.ecoscan.app.core.designsystem.component.EcoScanTopBar
import pe.ecoscan.app.core.designsystem.theme.EcoScanTheme
import pe.ecoscan.app.domain.model.PackagedProduct
import pe.ecoscan.app.domain.model.PackagingMaterialMapper
import pe.ecoscan.app.domain.model.WasteCategory

private const val NEUTRAL_MATERIAL_HINT =
    "No se reconoció este material. Consulta en el punto de acopio cómo separarlo."

private const val ATTRIBUTION_TEXT =
    "Datos de empaque de Open Food Facts (openfoodfacts.org), bajo licencia " +
        "Open Database License (ODbL)."

@Composable
fun ProductDetailRoute(
    barcode: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(barcode) {
        viewModel.consultar(barcode)
    }

    ProductDetailScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRetry = viewModel::reintentar,
        modifier = modifier
    )
}

@Composable
fun ProductDetailScreen(
    uiState: ProductUiState,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { EcoScanTopBar(title = "Ficha del envase", onNavigateBack = onNavigateBack) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> EcoScanLoading()

                uiState.error != null -> ProductErrorContent(
                    errorType = uiState.error,
                    onNavigateBack = onNavigateBack,
                    onRetry = onRetry
                )

                uiState.product != null -> ProductDetailContent(
                    product = uiState.product,
                    isFromCache = uiState.isFromCache
                )

                else -> EcoScanLoading()
            }
        }
    }
}

@Composable
private fun ProductErrorContent(
    errorType: ErrorType,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (errorType) {
        ErrorType.NOT_FOUND -> EcoScanEmptyState(
            message = "Este envase no está registrado en la base de datos comunitaria de " +
                "Open Food Facts.",
            actionLabel = "Volver a escanear",
            onAction = onNavigateBack,
            modifier = modifier
        )

        ErrorType.NO_CONNECTION -> EcoScanEmptyState(
            message = "Sin conexión a internet. Revisa tu conexión e intenta de nuevo.",
            actionLabel = "Reintentar",
            onAction = onRetry,
            modifier = modifier
        )

        ErrorType.TIMEOUT -> EcoScanEmptyState(
            message = "El servidor tardó demasiado en responder.",
            actionLabel = "Reintentar",
            onAction = onRetry,
            modifier = modifier
        )

        ErrorType.SERVER_ERROR, ErrorType.UNKNOWN -> EcoScanEmptyState(
            message = "Ocurrió un problema al consultar el producto. Intenta de nuevo más tarde.",
            actionLabel = "Reintentar",
            onAction = onRetry,
            modifier = modifier
        )
    }
}

@Composable
private fun ProductDetailContent(
    product: PackagedProduct,
    isFromCache: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isFromCache) {
            EcoScanCard {
                Text(
                    text = "Mostrando la última información guardada; no se pudo actualizar " +
                        "desde la red.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        EcoScanCard {
            product.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = product.name ?: "Imagen del envase",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = product.name ?: "Producto sin nombre registrado",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            product.brand?.let { brand ->
                Text(
                    text = brand,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        EcoScanCard {
            Text(
                text = "Materiales del empaque",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (product.packagingMaterials.isEmpty()) {
                Text(
                    text = product.disposalHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    product.packagingMaterials.forEach { material ->
                        MaterialRow(material = material)
                    }
                }
            }
        }

        Text(
            text = ATTRIBUTION_TEXT,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MaterialRow(material: String, modifier: Modifier = Modifier) {
    val category = PackagingMaterialMapper.categoryFor(material)
    val hint = PackagingMaterialMapper.hintFor(material) ?: NEUTRAL_MATERIAL_HINT
    val (containerColor, onContainerColor) = materialCategoryColors(category)

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            color = containerColor,
            contentColor = onContainerColor,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = material,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// Colores reutilizados del tema para distinguir cada categoría de residuo. Un material
// sin categoría única (ej. Tetrapak) usa el tono neutro de superficie.
@Composable
private fun materialCategoryColors(category: WasteCategory?): Pair<Color, Color> = when (category) {
    WasteCategory.PLASTIC ->
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    WasteCategory.GLASS ->
        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    WasteCategory.PAPER ->
        MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    WasteCategory.METAL ->
        MaterialTheme.colorScheme.inverseSurface to MaterialTheme.colorScheme.inverseOnSurface
    WasteCategory.ORGANIC ->
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    WasteCategory.HAZARDOUS ->
        MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    null ->
        MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
}

@PreviewLightDark
@Composable
private fun ProductDetailScreenSuccessPreview() {
    EcoScanTheme {
        ProductDetailScreen(
            uiState = ProductUiState(
                product = PackagedProduct(
                    barcode = "7751271014013",
                    name = "Agua Mineral San Luis",
                    brand = "San Luis",
                    packagingMaterials = listOf("Plástico", "Tetrapak"),
                    imageUrl = null,
                    disposalHint = "Enjuaga el envase y sepáralo como plástico."
                ),
                isFromCache = true
            ),
            onNavigateBack = {},
            onRetry = {}
        )
    }
}

@PreviewLightDark
@Composable
private fun ProductDetailScreenErrorPreview() {
    EcoScanTheme {
        ProductDetailScreen(
            uiState = ProductUiState(error = ErrorType.NO_CONNECTION),
            onNavigateBack = {},
            onRetry = {}
        )
    }
}
