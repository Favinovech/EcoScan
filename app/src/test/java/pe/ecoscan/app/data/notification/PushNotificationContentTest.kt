package pe.ecoscan.app.data.notification

import org.junit.Assert.assertEquals
import org.junit.Test

class PushNotificationContentTest {

    @Test
    fun `usa el titulo y cuerpo del bloque notification si vienen presentes`() {
        val content = buildPushNotificationContent(
            data = mapOf("title" to "Del data", "body" to "Cuerpo del data"),
            notificationTitle = "Título FCM",
            notificationBody = "Cuerpo FCM"
        )

        assertEquals("Título FCM", content.title)
        assertEquals("Cuerpo FCM", content.body)
    }

    @Test
    fun `sin bloque notification usa el titulo y cuerpo del Map de datos`() {
        val content = buildPushNotificationContent(
            data = mapOf("title" to "Nueva campaña", "body" to "Hay un punto de acopio nuevo"),
            notificationTitle = null,
            notificationBody = null
        )

        assertEquals("Nueva campaña", content.title)
        assertEquals("Hay un punto de acopio nuevo", content.body)
    }

    @Test
    fun `sin titulo ni cuerpo en ningun lado devuelve textos vacios`() {
        val content = buildPushNotificationContent(
            data = emptyMap(),
            notificationTitle = null,
            notificationBody = null
        )

        assertEquals("", content.title)
        assertEquals("", content.body)
    }
}
