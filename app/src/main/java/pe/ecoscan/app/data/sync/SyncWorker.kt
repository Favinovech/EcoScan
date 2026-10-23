package pe.ecoscan.app.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import pe.ecoscan.app.domain.repository.AuthRepository
import pe.ecoscan.app.domain.repository.ProfileRepository

// Sube a Firestore lo que quedó pendiente en Room (waste_records y el perfil) cuando
// WasteRecordSyncCoordinator no llegó a hacerlo porque la app estaba cerrada o en segundo
// plano. Reutiliza WasteRecordSyncer y ProfileRepository.syncPendingProfile: no duplica esa
// lógica, solo decide cuándo reintentar según el tipo de error.
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val authRepository: AuthRepository,
    private val wasteRecordSyncer: WasteRecordSyncer,
    private val profileRepository: ProfileRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val uid = authRepository.currentUser.first()?.uid ?: return Result.success()

        return try {
            val wasteResult = wasteRecordSyncer.sync(uid)
            val profileResult = profileRepository.syncPendingProfile(uid)
            val failure = wasteResult.exceptionOrNull() ?: profileResult.exceptionOrNull()

            when {
                failure == null -> Result.success()
                isTransientSyncError(failure) -> Result.retry()
                else -> Result.failure()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (isTransientSyncError(e)) Result.retry() else Result.failure()
        }
    }
}
