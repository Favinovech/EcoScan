package pe.ecoscan.app.core.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import android.os.Bundle
import javax.inject.Inject

// Abstrae Firebase para que el resto de la app nunca dependa directamente de Crashlytics
// ni de Analytics: solo conoce este contrato.
interface CrashReporter {
    fun logEvent(name: String, params: Map<String, String> = emptyMap())
    fun recordError(throwable: Throwable)
}

class FirebaseCrashReporter @Inject constructor(
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics
) : CrashReporter {

    override fun logEvent(name: String, params: Map<String, String>) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) -> putString(key, value) }
        }
        analytics.logEvent(name, bundle)
    }

    override fun recordError(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }
}
