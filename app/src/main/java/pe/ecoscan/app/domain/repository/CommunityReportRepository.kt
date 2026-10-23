package pe.ecoscan.app.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import pe.ecoscan.app.domain.model.CommunityReport

interface CommunityReportRepository {
    suspend fun createReport(report: CommunityReport, imageUri: Uri?): Result<Unit>
    fun getUserReports(userId: String): Flow<List<CommunityReport>>
    suspend fun voteReport(reportId: String, isConfirm: Boolean, userId: String): Result<Unit>
}