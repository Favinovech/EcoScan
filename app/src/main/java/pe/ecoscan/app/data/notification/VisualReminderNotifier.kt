package pe.ecoscan.app.data.notification

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import pe.ecoscan.app.R
import pe.ecoscan.app.domain.model.CollectionSchedule
import pe.ecoscan.app.domain.notification.ReminderNotifier
import javax.inject.Inject

private const val REMINDER_NOTIFICATION_ID = 1001

// Implementación real de ReminderNotifier (HU09, parte 2): reemplaza a la de solo log de la
// parte 1. Delega en NotificationPoster, que ya respeta el interruptor maestro del perfil y las
// preferencias de sonido/vibración; ReminderWorker no cambia, sigue llamando a notify() igual.
class VisualReminderNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationPoster: NotificationPoster
) : ReminderNotifier {

    override suspend fun notify(schedule: CollectionSchedule) {
        val time = "%02d:%02d".format(schedule.hour, schedule.minute)
        notificationPoster.post(
            notificationId = REMINDER_NOTIFICATION_ID,
            title = context.getString(R.string.notification_reminder_title),
            body = context.getString(R.string.notification_reminder_body, schedule.district, time)
        )
    }
}
