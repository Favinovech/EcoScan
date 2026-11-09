package pe.ecoscan.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.ecoscan.app.domain.model.BarChartItem
import pe.ecoscan.app.domain.model.ImpactFactors
import pe.ecoscan.app.domain.model.ImpactMetrics
import pe.ecoscan.app.domain.model.MaterialDistribution
import pe.ecoscan.app.domain.model.TimeRangeFilter
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteRecord
import pe.ecoscan.app.domain.repository.WasteRecordRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class DashboardData(
    val metrics: ImpactMetrics,
    val materialDistribution: List<MaterialDistribution>,
    val barChartData: List<BarChartItem>
)

class GetEnvironmentalImpactUseCase @Inject constructor(
    private val wasteRecordRepository: WasteRecordRepository
) {
    operator fun invoke(timeRange: TimeRangeFilter): Flow<DashboardData> {
        return wasteRecordRepository.getWasteRecords().map { records ->
            calculateDashboardData(records, timeRange)
        }
    }

    private fun calculateDashboardData(
        records: List<WasteRecord>,
        timeRange: TimeRangeFilter
    ): DashboardData {
        var totalWeight = 0.0
        var totalCo2 = 0.0
        var totalWater = 0.0

        val categoryWeightMap = mutableMapOf<WasteCategory, Double>()

        records.forEach { record ->
            val weight = record.weightKg
            totalWeight += weight

            val (co2Factor, waterFactor) = ImpactFactors.getFactors(record.category)
            totalCo2 += weight * co2Factor
            totalWater += weight * waterFactor

            categoryWeightMap[record.category] = (categoryWeightMap[record.category] ?: 0.0) + weight
        }

        val distribution = categoryWeightMap.map { (cat, weight) ->
            MaterialDistribution(
                category = cat,
                totalKg = weight,
                percentage = if (totalWeight > 0) ((weight / totalWeight) * 100).toFloat() else 0f
            )
        }.sortedByDescending { it.totalKg }

        val barItems = when (timeRange) {
            TimeRangeFilter.WEEKLY -> groupRecordsByDayOfWeek(records)
            TimeRangeFilter.MONTHLY -> groupRecordsByWeekOfMonth(records)
        }

        return DashboardData(
            metrics = ImpactMetrics(
                totalWeightKg = totalWeight,
                co2SavedKg = totalCo2,
                waterSavedLiters = totalWater,
                communityAvgKg = 4.2
            ),
            materialDistribution = distribution,
            barChartData = barItems
        )
    }

    private fun groupRecordsByDayOfWeek(records: List<WasteRecord>): List<BarChartItem> {
        val days = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        val map = days.associateWith { 0f }.toMutableMap()
        val sdf = SimpleDateFormat("EEE", Locale("es", "ES"))

        records.forEach { record ->
            val dayName = sdf.format(Date(record.recordedAt)).replace(".", "").replaceFirstChar { it.uppercase() }
            if (map.containsKey(dayName)) {
                map[dayName] = (map[dayName] ?: 0f) + record.weightKg.toFloat()
            }
        }
        return map.map { BarChartItem(it.key, it.value) }
    }

    private fun groupRecordsByWeekOfMonth(records: List<WasteRecord>): List<BarChartItem> {
        val weeks = listOf("Sem 1", "Sem 2", "Sem 3", "Sem 4")
        val map = weeks.associateWith { 0f }.toMutableMap()
        val cal = Calendar.getInstance()

        records.forEach { record ->
            cal.timeInMillis = record.recordedAt
            val weekIndex = ((cal.get(Calendar.DAY_OF_MONTH) - 1) / 7).coerceIn(0, 3)
            val weekKey = "Sem ${weekIndex + 1}"
            if (map.containsKey(weekKey)) {
                map[weekKey] = (map[weekKey] ?: 0f) + record.weightKg.toFloat()
            }
        }
        return map.map { BarChartItem(it.key, it.value) }
    }
}