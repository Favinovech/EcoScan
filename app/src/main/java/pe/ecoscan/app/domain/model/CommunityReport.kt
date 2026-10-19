package pe.ecoscan.app.domain.model

data class CommunityReport(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "", // Plástico, Vidrio, Metal, etc.
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String = "",
    val reportType: ReportType = ReportType.NEW_POINT,
    val confirmVotes: Int = 0,
    val rejectVotes: Int = 0,
    val isApproved: Boolean = false, // Pasa a true al alcanzar 3 votos de confirmación
    val timestamp: Long = System.currentTimeMillis()
)