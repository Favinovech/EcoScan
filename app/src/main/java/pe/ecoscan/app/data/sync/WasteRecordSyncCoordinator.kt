package pe.ecoscan.app.data.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.core.network.ConnectivityObserver
import pe.ecoscan.app.data.local.dao.WasteRecordDao
import pe.ecoscan.app.domain.repository.AuthRepository
import pe.ecoscan.app.domain.sync.WasteRecordSyncer
import javax.inject.Inject
import javax.inject.Singleton

// Dispara la sincronización con Firestore en tres momentos: al iniciar sesión con internet,
// cada vez que el dispositivo recupera la conexión y cada vez que hay cambios locales
// pendientes (nuevo escaneo, edición o eliminación). Se inicia una sola vez desde
// EcoScanApplication.
@Singleton
class WasteRecordSyncCoordinator @Inject constructor(
    private val authRepository: AuthRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val dao: WasteRecordDao,
    private val syncer: WasteRecordSyncer,
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
                if (uid == null) {
                    emptyFlow<Pair<String, Boolean>>()
                } else {
                    val hasPending = dao.observePendingCount(uid).map { it > 0 }.distinctUntilChanged()
                    combine(connectivityObserver.isOnline, hasPending) { online, _ -> uid to online }
                }
            }
            .filter { (_, online) -> online }
            .onEach { (uid, _) -> syncer.sync(uid) }
            .launchIn(scope)
    }
}