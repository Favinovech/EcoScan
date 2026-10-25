package pe.ecoscan.app.domain.notification

import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationChannelSelectorTest {

    @Test
    fun `sonido y vibracion activos devuelve el canal con ambos`() {
        val channelId = selectNotificationChannelId(soundEnabled = true, vibrationEnabled = true)

        assertEquals(NotificationChannelIds.SOUND_AND_VIBRATION, channelId)
    }

    @Test
    fun `solo vibracion activa devuelve el canal de solo vibracion`() {
        val channelId = selectNotificationChannelId(soundEnabled = false, vibrationEnabled = true)

        assertEquals(NotificationChannelIds.VIBRATION_ONLY, channelId)
    }

    @Test
    fun `solo sonido activo devuelve el canal de solo sonido`() {
        val channelId = selectNotificationChannelId(soundEnabled = true, vibrationEnabled = false)

        assertEquals(NotificationChannelIds.SOUND_ONLY, channelId)
    }

    @Test
    fun `ninguno activo devuelve el canal silencioso`() {
        val channelId = selectNotificationChannelId(soundEnabled = false, vibrationEnabled = false)

        assertEquals(NotificationChannelIds.SILENT, channelId)
    }
}
