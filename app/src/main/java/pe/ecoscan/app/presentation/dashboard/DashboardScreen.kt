package pe.ecoscan.app.presentation.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.ecoscan.app.domain.model.BarChartItem
import pe.ecoscan.app.domain.model.MaterialDistribution
import pe.ecoscan.app.domain.model.TimeRangeFilter
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.usecase.DashboardData

@Composable
fun DashboardRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    DashboardScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onTimeRangeSelected = viewModel::onTimeRangeSelected,
        onExportClick = { viewModel.exportAndShareReport(context) },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onNavigateBack: () -> Unit,
    onTimeRangeSelected: (TimeRangeFilter) -> Unit,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Impacto Ambiental", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onExportClick) {
                        Icon(Icons.Default.Share, contentDescription = "Exportar y compartir reporte")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when (uiState) {
            is DashboardUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is DashboardUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is DashboardUiState.Success -> {
                DashboardContent(
                    data = uiState.data,
                    selectedRange = uiState.selectedTimeRange,
                    onTimeRangeSelected = onTimeRangeSelected,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    data: DashboardData,
    selectedRange: TimeRangeFilter,
    onTimeRangeSelected: (TimeRangeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Selector Semanal / Mensual
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedRange == TimeRangeFilter.WEEKLY,
                onClick = { onTimeRangeSelected(TimeRangeFilter.WEEKLY) },
                label = { Text("Semanal") }
            )
            FilterChip(
                selected = selectedRange == TimeRangeFilter.MONTHLY,
                onClick = { onTimeRangeSelected(TimeRangeFilter.MONTHLY) },
                label = { Text("Mensual") }
            )
        }

        // Tarjetas de Métricas Principales (CO2 y Agua)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "CO₂ Evitado",
                value = String.format("%.2f kg", data.metrics.co2SavedKg),
                subtitle = "Equivalente a huella de carbono",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Agua Ahorrada",
                value = String.format("%.1f L", data.metrics.waterSavedLiters),
                subtitle = "Litros conservados",
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }

        // Comparativa con la comunidad
        CommunityComparisonCard(
            userWeight = data.metrics.totalWeightKg,
            communityAvg = data.metrics.communityAvgKg
        )

        // Gráfico de Barras
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Kilogramos Reciclados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                BarChart(items = data.barChartData)
            }
        }

        // Gráfico Circular de Materiales
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Distribución por Material",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                DonutChart(distribution = data.materialDistribution)
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CommunityComparisonCard(userWeight: Double, communityAvg: Double) {
    val progress = if (communityAvg > 0) (userWeight / (communityAvg * 1.5)).coerceIn(0.0, 1.0).toFloat() else 0f
    val isAhead = userWeight >= communityAvg

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Comparativa con la Comunidad", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (isAhead) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tú: ${String.format("%.2f", userWeight)} kg", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text("Promedio: ${String.format("%.2f", communityAvg)} kg", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun BarChart(items: List<BarChartItem>) {
    val maxWeight = items.maxOfOrNull { it.weightKg }?.takeIf { it > 0 } ?: 1f
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        items.forEach { item ->
            val barHeightFraction = (item.weightKg / maxWeight).coerceIn(0.05f, 1f)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = if (item.weightKg > 0) String.format("%.1f", item.weightKg) else "",
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Canvas(
                    modifier = Modifier
                        .width(20.dp)
                        .height(100.dp * barHeightFraction)
                ) {
                    drawRoundRect(
                        color = primaryColor,
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(item.label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun DonutChart(distribution: List<MaterialDistribution>) {
    val categoryColors = mapOf(
        WasteCategory.PLASTIC to Color(0xFF1E88E5),
        WasteCategory.GLASS to Color(0xFF26A69A),
        WasteCategory.PAPER to Color(0xFFFFA000),
        WasteCategory.METAL to Color(0xFF78909C),
        WasteCategory.ORGANIC to Color(0xFF66BB6A),
        WasteCategory.HAZARDOUS to Color(0xFFE53935)
    )

    if (distribution.isEmpty()) {
        Text("No hay datos de distribución registrados aún.", style = MaterialTheme.typography.bodySmall)
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Donut
        Box(
            modifier = Modifier
                .size(130.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                distribution.forEach { item ->
                    val sweepAngle = (item.percentage / 100f) * 360f
                    val color = categoryColors[item.category] ?: Color.Gray
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 24f, cap = StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Leyenda
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            distribution.forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(categoryColors[item.category] ?: Color.Gray, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.category.displayName}: ${String.format("%.1f", item.percentage)}% (${String.format("%.1f", item.totalKg)} kg)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}