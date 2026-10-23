package pe.ecoscan.app.data.notification

import android.util.Log
import pe.ecoscan.app.domain.model.CollectionSchedule
import pe.ecoscan.app.domain.notification.ReminderNotifier
import javax.inject.Inject

private const val TAG = "ReminderNotifier"

// Implementación provisional de ReminderNotifier: solo registra en el log. La parte 2 de la
// HU09 la reemplaza por una notificación visual real (con sus propias preferencias de sonido
// y vibración), sin tener que tocar el Worker que la invoca.
class LoggingReminderNotifier @Inject constructor() : ReminderNotifier {

    override fun notify(schedule: CollectionSchedule) {
        val time = "%02d:%02d".format(schedule.hour, schedule.minute)
        Log.i(TAG, "Recordatorio: hoy pasa la recolección en ${schedule.district} (${schedule.dayOfWeek}, $time)")
    }
}
