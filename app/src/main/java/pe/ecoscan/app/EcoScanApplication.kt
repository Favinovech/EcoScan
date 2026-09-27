package pe.ecoscan.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp

// Clase Application que habilita el contenedor de dependencias de Hilt para toda la app.
@HiltAndroidApp
class EcoScanApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Sin app/google-services.json (ver app/build.gradle.kts) Firebase nunca se
        // inicializa; evitamos el crash y solo activamos Crashlytics cuando sí está listo.
        // La recolección solo se activa en release: en debug no queremos reportar los
        // crashes de desarrollo local.
        if (FirebaseApp.getApps(this).isNotEmpty()) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        }
    }
}
