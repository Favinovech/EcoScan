package pe.ecoscan.app.data.repository

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.data.remote.dto.CommunityReportDto
import pe.ecoscan.app.domain.model.CommunityReport
import pe.ecoscan.app.domain.repository.CommunityReportRepository
import java.util.UUID
import javax.inject.Inject

class CommunityReportRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val dispatchers: DispatcherProvider
) : CommunityReportRepository {

    private val reportsCollection = firestore.collection("community_reports")

    override suspend fun createReport(report: CommunityReport, imageUri: Uri?): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            val reportId = UUID.randomUUID().toString()
            var finalImageUrl = ""

            // 1. Subida a Firebase Storage si se adjuntó foto
            if (imageUri != null) {
                val imageRef = storage.reference.child("reports/$reportId.jpg")
                imageRef.putFile(imageUri).await()
                finalImageUrl = imageRef.downloadUrl.await().toString()
            }

            // 2. Persistencia en Cloud Firestore
            val dto = CommunityReportDto.fromDomain(
                report = report.copy(id = reportId, imageUrl = finalImageUrl)
            )
            reportsCollection.document(reportId).set(dto).await()
            Unit // 👈 Retorna Unit explícitamente para cumplir con Result<Unit>
        }
    }

    override fun getUserReports(userId: String): Flow<List<CommunityReport>> {
        return reportsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(CommunityReportDto::class.java)?.toDomain()
                }
            }
    }

    override suspend fun voteReport(reportId: String, isConfirm: Boolean, userId: String): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            val docRef = reportsCollection.document(reportId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val votedUsers = snapshot.get("votedUserIds") as? List<*> ?: emptyList<Any>()

                if (votedUsers.contains(userId)) {
                    throw IllegalStateException("Ya has votado en este reporte.")
                }

                val currentConfirm = snapshot.getLong("confirmVotes")?.toInt() ?: 0
                val currentReject = snapshot.getLong("rejectVotes")?.toInt() ?: 0

                val newConfirm = if (isConfirm) currentConfirm + 1 else currentConfirm
                val newReject = if (!isConfirm) currentReject + 1 else currentReject
                val shouldApprove = newConfirm >= 3 // Criterio HU08: se aprueba con 3 votos

                transaction.update(docRef, "confirmVotes", newConfirm)
                transaction.update(docRef, "rejectVotes", newReject)
                transaction.update(docRef, "isApproved", shouldApprove)
                transaction.update(docRef, "votedUserIds", FieldValue.arrayUnion(userId))
            }.await()
            Unit // 👈 Retorna Unit explícitamente para cumplir con Result<Unit>
        }
    }
}