package pe.ecoscan.app.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import pe.ecoscan.app.R
import pe.ecoscan.app.domain.notification.NotificationChannelIds

// Crea los 4 canales fijos de EcoScan. Se llama desde EcoScanApplication antes de mostrar
// cualquier notificación. createNotificationChannel es idempotente: si el canal ya existe con
// el mismo ID, Android no lo recrea ni cambia su sonido/vibración, así que es seguro llamarlo
// en cada arranque de la app.
object NotificationChannels {

    fun createAll(context: Context) {
        val manager = NotificationManagerCompat.from(context)
        manager.createNotificationChannel(
            channel(
                context = context,
                id = NotificationChannelIds.SOUND_AND_VIBRATION,
                nameRes = R.string.notification_channel_sound_and_vibration,
                sound = true,
                vibration = true
            )
        )
        manager.createNotificationChannel(
            channel(
                context = context,
                id = NotificationChannelIds.VIBRATION_ONLY,
                nameRes = R.string.notification_channel_vibration_only,
                sound = false,
                vibration = true
            )
        )
        manager.createNotificationChannel(
            channel(
                context = context,
                id = NotificationChannelIds.SOUND_ONLY,
                nameRes = R.string.notification_channel_sound_only,
                sound = true,
                vibration = false
            )
        )
        manager.createNotificationChannel(
            channel(
                context = context,
                id = NotificationChannelIds.SILENT,
                nameRes = R.string.notification_channel_silent,
                sound = false,
                vibration = false
            )
        )
    }

    private fun channel(
        context: Context,
        id: String,
        nameRes: Int,
        sound: Boolean,
        vibration: Boolean
    ): NotificationChannel =
        NotificationChannel(id, context.getString(nameRes), NotificationManager.IMPORTANCE_DEFAULT).apply {
            enableVibration(vibration)
            if (!sound) setSound(null, null)
        }
}
