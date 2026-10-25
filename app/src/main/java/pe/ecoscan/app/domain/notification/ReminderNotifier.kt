package pe.ecoscan.app.domain.notification

import pe.ecoscan.app.domain.model.CollectionSchedule

// La parte 2 de HU09 implementa esta interfaz con la notificación visual real
// (NotificationCompat/FCM, preferencias de sonido y vibración). notify() es suspend porque esa
// implementación necesita leer de forma asíncrona el interruptor maestro del perfil (Room) y las
// preferencias de sonido/vibración (DataStore) antes de mostrar la notificación.
interface ReminderNotifier {
    suspend fun notify(schedule: CollectionSchedule)
}
