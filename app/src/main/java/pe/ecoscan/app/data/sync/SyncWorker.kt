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
import pe.ecoscan.app.domain.sync.RetryDecision
import pe.ecoscan.app.domain.sync.WasteRecordSyncer
import pe.ecoscan.app.domain.sync.decideRetry

// Sube a Firestore lo que quedó pendiente en Room (waste_records y el perfil) cuando
// WasteRecordSyncCoordinator no llegó a hacerlo porque la app estaba cerrada o en segundo
// plano. Reutiliza WasteRecordSyncer y ProfileRepository.syncPendingProfile: no duplica esa
// lógica, solo decide cuándo reintentar según el tipo de error y el número de intento
// (domain/sync/RetryPolicy, que además tiene un tope para no reintentar para siempre).
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
            resultFor(failure)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            resultFor(e)
        }
    }

    // runAttemptCount empieza en 0 para el primer intento; decideRetry espera el número del
    // intento que acaba de fallar (1 = el primero), de ahí el +1.
    private fun resultFor(failure: Throwable?): Result {
        if (failure == null) return Result.success()
        return when (decideRetry(classifySyncError(failure), attempt = runAttemptCount + 1)) {
            RetryDecision.Retry -> Result.retry()
            RetryDecision.Fail -> Result.failure()
        }
    }
}
