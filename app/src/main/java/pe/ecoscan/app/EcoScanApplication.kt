package pe.ecoscan.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import pe.ecoscan.app.data.reminder.ReminderCoordinator
import pe.ecoscan.app.data.sync.SyncScheduler
import pe.ecoscan.app.data.sync.WasteRecordSyncCoordinator
import javax.inject.Inject

// Clase Application que habilita el contenedor de dependencias de Hilt para toda la app.
@HiltAndroidApp
class EcoScanApplication : Application(), Configuration.Provider {

    // Lazy: estos coordinadores necesitan Auth/Firestore, que solo existen si Firebase se
    // inicializó (ver comentario en onCreate). HiltWorkerFactory no entra en ese caso: no
    // construye los Worker ni sus dependencias hasta que WorkManager los ejecuta.
    @Inject
    lateinit var syncCoordinator: Lazy<WasteRecordSyncCoordinator>

    @Inject
    lateinit var reminderCoordinator: Lazy<ReminderCoordinator>

    @Inject
    lateinit var syncScheduler: Lazy<SyncScheduler>

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // WorkManager llama a esto a demanda en su primer getInstance(): por eso se quitó el
    // inicializador automático de androidx.startup en el AndroidManifest.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Sin app/google-services.json (ver app/build.gradle.kts) Firebase nunca se
        // inicializa; evitamos el crash y solo activamos Crashlytics cuando sí está listo.
        // La recolección solo se activa en release: en debug no queremos reportar los
        // crashes de desarrollo local.
        if (FirebaseApp.getApps(this).isNotEmpty()) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
            // HU06: sincroniza el historial con Firestore al haber sesión e internet.
            syncCoordinator.get().start()
            // HU09: reprograma el recordatorio de recolección cuando cambia el distrito, y
            // deja un respaldo periódico que sube lo pendiente aunque la app esté cerrada.
            reminderCoordinator.get().start()
            syncScheduler.get().schedulePeriodicBackup()
        }
    }
}