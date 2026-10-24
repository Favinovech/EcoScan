package pe.ecoscan.app.data.notification

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.domain.repository.AuthRepository
import pe.ecoscan.app.domain.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

private const val TOPIC_CAMPAIGNS = "campanas"
private const val TOPIC_ACHIEVEMENTS = "logros"

// Suscribe o cancela los topics de FCM según el interruptor maestro del perfil (HU09, parte 2):
// activo suscribe a "campanas" y "logros", inactivo cancela ambas. Se inicia una sola vez desde
// EcoScanApplication, igual que ReminderCoordinator, y solo si Firebase ya se inicializó.
@Singleton
class NotificationTopicManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    dispatcherProvider: DispatcherProvider
) {

    private val scope = CoroutineScope(SupervisorJob() + dispatcherProvider.default)
    private var started = false

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
        if (started) return
        started = true

        authRepository.currentUser
            .map { user -> user?.uid }
            .distinctUntilChanged()
            .flatMapLatest { uid ->
                if (uid == null) emptyFlow() else profileRepository.observeProfile(uid)
            }
            .map { profile -> profile?.notificationsEnabled ?: false }
            .distinctUntilChanged()
            .onEach { enabled -> if (enabled) subscribe() else unsubscribe() }
            .launchIn(scope)
    }

    private fun subscribe() {
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_CAMPAIGNS)
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC_ACHIEVEMENTS)
    }

    private fun unsubscribe() {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_CAMPAIGNS)
        FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC_ACHIEVEMENTS)
    }
}
