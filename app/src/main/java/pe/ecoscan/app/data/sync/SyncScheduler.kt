package pe.ecoscan.app.data.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

private const val ONE_TIME_WORK_NAME = "sync_pending_records"
private const val PERIODIC_WORK_NAME = "sync_pending_records_periodic"
private val BACKOFF_DELAY = Duration.ofSeconds(30)

// Encola SyncWorker, siempre con la restricción de red conectada.
// - enqueuePendingSync(): se llama cada vez que queda un registro pendiente (isSynced = false).
//   ExistingWorkPolicy.KEEP evita acumular una sincronización por cada registro si se guardan
//   varios seguidos sin conexión.
// - schedulePeriodicBackup(): respaldo cada hora para el caso en que la tarea puntual no llegó
//   a encolarse (por ejemplo, la app se cerró antes de intentarlo).
@Singleton
class SyncScheduler @Inject constructor(
    private val workManager: WorkManager
) {

    private val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun enqueuePendingSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY)
            .build()
        workManager.enqueueUniqueWork(ONE_TIME_WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }

    fun schedulePeriodicBackup() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(Duration.ofHours(1))
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY)
            .build()
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
