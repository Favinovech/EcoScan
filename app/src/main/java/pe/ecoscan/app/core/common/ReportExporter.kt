package pe.ecoscan.app.core.common

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import pe.ecoscan.app.domain.usecase.DashboardData
import java.io.File
import java.io.FileWriter

object ReportExporter {
    fun exportToCsvAndShare(context: Context, data: DashboardData) {
        val file = File(context.cacheDir, "Reporte_Impacto_EcoScan.csv")
        FileWriter(file).use { writer ->
            writer.append("Métrica,Valor\n")
            writer.append("Total Reciclado (kg),${String.format("%.2f", data.metrics.totalWeightKg)}\n")
            writer.append("CO2 Evitado (kg),${String.format("%.2f", data.metrics.co2SavedKg)}\n")
            writer.append("Agua Ahorrada (Litros),${String.format("%.2f", data.metrics.waterSavedLiters)}\n")
            writer.append("\nMaterial,Kilogramos,Porcentaje\n")
            data.materialDistribution.forEach { item ->
                writer.append("${item.category.name},${String.format("%.2f", item.totalKg)},${String.format("%.1f", item.percentage)}%\n")
            }
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Mi Impacto Ambiental en EcoScan")
            putExtra(Intent.EXTRA_TEXT, "He reciclado ${String.format("%.1f", data.metrics.totalWeightKg)} kg con EcoScan, evitando ${String.format("%.1f", data.metrics.co2SavedKg)} kg de CO2.")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir reporte de impacto"))
    }
}