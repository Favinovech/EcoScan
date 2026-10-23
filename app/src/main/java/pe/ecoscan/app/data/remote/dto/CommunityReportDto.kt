package pe.ecoscan.app.data.remote.dto

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import pe.ecoscan.app.domain.model.CommunityReport
import pe.ecoscan.app.domain.model.ReportType
import java.util.Date

data class CommunityReportDto(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String = "",
    val reportType: String = ReportType.NEW_POINT.name,
    val confirmVotes: Int = 0,
    val rejectVotes: Int = 0,
    val isApproved: Boolean = false,
    val votedUserIds: List<String> = emptyList(), // Control de un voto por usuario
    @ServerTimestamp
    val createdAt: Date? = null
) {
    fun toDomain(): CommunityReport {
        return CommunityReport(
            id = id,
            userId = userId,
            title = title,
            description = description,
            category = category,
            latitude = latitude,
            longitude = longitude,
            imageUrl = imageUrl,
            reportType = runCatching { ReportType.valueOf(reportType) }.getOrDefault(ReportType.NEW_POINT),
            confirmVotes = confirmVotes,
            rejectVotes = rejectVotes,
            isApproved = isApproved,
            timestamp = createdAt?.time ?: System.currentTimeMillis()
        )
    }

    companion object {
        fun fromDomain(report: CommunityReport, votedUsers: List<String> = emptyList()): CommunityReportDto {
            return CommunityReportDto(
                id = report.id,
                userId = report.userId,
                title = report.title,
                description = report.description,
                category = report.category,
                latitude = report.latitude,
                longitude = report.longitude,
                imageUrl = report.imageUrl,
                reportType = report.reportType.name,
                confirmVotes = report.confirmVotes,
                rejectVotes = report.rejectVotes,
                isApproved = report.isApproved,
                votedUserIds = votedUsers
            )
        }
    }
}