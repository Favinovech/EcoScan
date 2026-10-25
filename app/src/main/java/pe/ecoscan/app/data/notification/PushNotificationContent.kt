package pe.ecoscan.app.data.notification

// Texto final de una notificación de FCM (campañas o logros).
data class PushNotificationContent(val title: String, val body: String)

// Construye el contenido a partir del Map de datos del mensaje y, si lo trae, el bloque
// "notification" (título/cuerpo). Recibe estos valores ya extraídos en vez de un RemoteMessage
// para poder probarla con JUnit normal, sin depender de Firebase.
fun buildPushNotificationContent(
    data: Map<String, String>,
    notificationTitle: String?,
    notificationBody: String?
): PushNotificationContent = PushNotificationContent(
    title = notificationTitle ?: data["title"].orEmpty(),
    body = notificationBody ?: data["body"].orEmpty()
)
