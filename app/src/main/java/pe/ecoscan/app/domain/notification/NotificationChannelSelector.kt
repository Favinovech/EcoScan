package pe.ecoscan.app.domain.notification

// IDs de los 4 canales de notificación fijos de EcoScan (HU09, parte 2). Android no permite
// cambiar el sonido ni la vibración de un canal después de crearlo, así que en vez de un canal
// configurable existe uno por cada combinación posible.
object NotificationChannelIds {
    const val SOUND_AND_VIBRATION = "ecoscan_sonido_vibracion"
    const val VIBRATION_ONLY = "ecoscan_solo_vibracion"
    const val SOUND_ONLY = "ecoscan_solo_sonido"
    const val SILENT = "ecoscan_silencioso"
}

// Función pura (sin Android) para poder probarla con JUnit normal, igual que
// calculateInitialDelayMillis en domain/schedule.
fun selectNotificationChannelId(soundEnabled: Boolean, vibrationEnabled: Boolean): String = when {
    soundEnabled && vibrationEnabled -> NotificationChannelIds.SOUND_AND_VIBRATION
    vibrationEnabled -> NotificationChannelIds.VIBRATION_ONLY
    soundEnabled -> NotificationChannelIds.SOUND_ONLY
    else -> NotificationChannelIds.SILENT
}
