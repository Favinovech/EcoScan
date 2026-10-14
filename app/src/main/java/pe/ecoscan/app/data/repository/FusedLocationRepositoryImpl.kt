package pe.ecoscan.app.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.domain.model.GeoPoint
import pe.ecoscan.app.domain.repository.LocationRepository
import javax.inject.Inject
import javax.inject.Singleton

private const val LOCATION_TIMEOUT_MS = 15_000L

// Ubicación con FusedLocationProviderClient (Google Play Services). Pide una posición nueva
// con precisión equilibrada (suficiente para buscar puntos cercanos y gasta poca batería) y,
// si no la consigue, usa la última conocida.
@Singleton
class FusedLocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedClient: FusedLocationProviderClient,
    private val dispatcherProvider: DispatcherProvider
) : LocationRepository {

    override suspend fun getCurrentLocation(): Result<GeoPoint> = withContext(dispatcherProvider.io) {
        if (!hasLocationPermission()) {
            return@withContext Result.failure(SecurityException("Falta el permiso de ubicación."))
        }
        try {
            val location = withTimeoutOrNull(LOCATION_TIMEOUT_MS) { requestLocation() }
            if (location != null) {
                Result.success(GeoPoint(location.latitude, location.longitude))
            } else {
                Result.failure(
                    IllegalStateException("No se pudo obtener tu ubicación. Activa el GPS e inténtalo de nuevo.")
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: SecurityException) {
            Result.failure(SecurityException("Falta el permiso de ubicación."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun requestLocation(): Location? {
        val tokenSource = CancellationTokenSource()
        val current: Location? = fusedClient
            .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
            .await(tokenSource)
        return current ?: fusedClient.lastLocation.await()
    }

    private fun hasLocationPermission(): Boolean =
        isGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
                isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)

    private fun isGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}