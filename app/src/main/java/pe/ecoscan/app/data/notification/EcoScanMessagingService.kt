package pe.ecoscan.app.data.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pe.ecoscan.app.BuildConfig
import javax.inject.Inject

private const val TAG = "EcoScanMessagingService"

// Recibe los mensajes de FCM de los topics "campanas" y "logros" (no hay servidor propio: se
// envían desde la consola de Firebase).
//
// Limitación de FCM: si el mensaje trae un bloque "notification" y la app está en segundo plano,
// el sistema la muestra directamente con el canal indicado en el mensaje o, si no trae ninguno,
// con el canal por defecto declarado en el manifest. onMessageReceived no se llama en ese caso,
// así que esta clase no interviene: solo entra en juego con la app en primer plano o con
// mensajes de solo datos.
@AndroidEntryPoint
class EcoScanMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationPoster: NotificationPoster

    private val scope = CoroutineScope(SupervisorJob())

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val content = buildPushNotificationContent(
            data = message.data,
            notificationTitle = message.notification?.title,
            notificationBody = message.notification?.body
        )

        scope.launch {
            notificationPoster.post(
                notificationId = System.currentTimeMillis().toInt(),
                title = content.title,
                body = content.body
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Nuevo token FCM: $token")
        }
    }
}
