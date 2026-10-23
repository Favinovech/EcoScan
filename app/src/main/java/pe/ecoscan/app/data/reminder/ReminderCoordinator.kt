package pe.ecoscan.app.data.reminder

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.domain.repository.AuthRepository
import pe.ecoscan.app.domain.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

// Reprograma el recordatorio periódico cada vez que cambia el distrito del perfil (con sesión
// iniciada). Se inicia una sola vez desde EcoScanApplication, igual que WasteRecordSyncCoordinator.
@Singleton
class ReminderCoordinator @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val reminderScheduler: ReminderScheduler,
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
            .map { profile -> profile?.district?.takeIf { it.isNotBlank() } }
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { district -> reminderScheduler.scheduleFor(district) }
            .launchIn(scope)
    }
}
