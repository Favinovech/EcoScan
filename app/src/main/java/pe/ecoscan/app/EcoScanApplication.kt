package pe.ecoscan.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Lazy
import dagger.hilt.android.HiltAndroidApp
import pe.ecoscan.app.data.sync.WasteRecordSyncCoordinator
import javax.inject.Inject

// Clase Application que habilita el contenedor de dependencias de Hilt para toda la app.
@HiltAndroidApp
class EcoScanApplication : Application() {

    // Lazy: el coordinador necesita Firestore, que solo existe si Firebase se inicializó.
    @Inject
    lateinit var syncCoordinator: Lazy<WasteRecordSyncCoordinator>

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
        }
    }
}