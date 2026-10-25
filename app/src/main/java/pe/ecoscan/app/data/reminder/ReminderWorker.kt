package pe.ecoscan.app.data.reminder

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import pe.ecoscan.app.domain.notification.ReminderNotifier
import pe.ecoscan.app.domain.repository.AuthRepository
import pe.ecoscan.app.domain.repository.CollectionScheduleRepository
import pe.ecoscan.app.domain.repository.ProfileRepository

// Avisa del día de recolección del distrito del usuario. Se encola como PeriodicWorkRequest
// cada 7 días (ReminderScheduler calcula el retraso inicial hasta la próxima ocurrencia), o
// como OneTimeWorkRequest desde el botón "Probar recordatorio" en builds debug.
//
// WorkManager no garantiza la hora exacta de ejecución: Doze y el ahorro de batería pueden
// retrasarla, así que el aviso puede llegar algo después de la hora programada del distrito.
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val collectionScheduleRepository: CollectionScheduleRepository,
    private val reminderNotifier: ReminderNotifier
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val uid = authRepository.currentUser.first()?.uid ?: return Result.success()
        val district = profileRepository.observeProfile(uid).first()?.district
        if (district.isNullOrBlank()) return Result.success()

        reminderNotifier.notify(collectionScheduleRepository.getSchedule(district))
        return Result.success()
    }
}
