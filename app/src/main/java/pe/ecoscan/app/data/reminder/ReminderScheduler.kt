package pe.ecoscan.app.data.reminder

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import pe.ecoscan.app.domain.repository.CollectionScheduleRepository
import pe.ecoscan.app.domain.schedule.calculateInitialDelayMillis
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val PERIODIC_WORK_NAME = "reminder_periodic_work"
private const val REMINDER_PERIOD_DAYS = 7L

// Encola (o reprograma) el recordatorio periódico según el distrito del perfil.
// ExistingPeriodicWorkPolicy.UPDATE reemplaza el trabajo existente con el nuevo horario en vez
// de encolar uno adicional, así que cambiar de distrito no deja trabajos duplicados corriendo.
@Singleton
class ReminderScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val collectionScheduleRepository: CollectionScheduleRepository
) {

    fun scheduleFor(district: String) {
        val schedule = collectionScheduleRepository.getSchedule(district)
        val initialDelayMillis = calculateInitialDelayMillis(
            schedule = schedule,
            nowMillis = System.currentTimeMillis(),
            zone = ZoneId.systemDefault()
        )

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(REMINDER_PERIOD_DAYS, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    // Botón "Probar recordatorio" (solo en builds debug): ejecuta el worker ya, sin esperar a
    // la próxima ocurrencia programada.
    fun scheduleTestReminder() {
        workManager.enqueue(OneTimeWorkRequestBuilder<ReminderWorker>().build())
    }
}
