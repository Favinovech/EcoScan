package pe.ecoscan.app.domain.notification

import pe.ecoscan.app.domain.model.CollectionSchedule

// La parte 2 de HU09 implementa esta interfaz con la notificación visual real
// (NotificationCompat/FCM, preferencias de sonido y vibración). En esta parte solo existe
// una implementación que registra en el log, para poder probar el flujo de extremo a extremo.
interface ReminderNotifier {
    fun notify(schedule: CollectionSchedule)
}
