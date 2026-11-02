package pe.ecoscan.app.data.repository

import android.util.Log
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.core.common.ErrorType
import pe.ecoscan.app.core.common.Resource
import pe.ecoscan.app.data.local.dao.CachedCollectionPointDao
import pe.ecoscan.app.data.mapper.toDomain
import pe.ecoscan.app.data.mapper.toEntity
import pe.ecoscan.app.data.remote.api.OverpassApi
import pe.ecoscan.app.data.remote.dto.OverpassResponseDto
import pe.ecoscan.app.domain.model.CollectionPoint
import pe.ecoscan.app.domain.model.GeoPoint
import pe.ecoscan.app.domain.repository.CollectionPointRepository
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.PI

private val CACHE_TTL_MILLIS = TimeUnit.DAYS.toMillis(30)
private const val METERS_PER_DEGREE = 111_320.0
private const val HTTP_TOO_MANY_REQUESTS = 429
private const val TAG = "EcoScanOverpass"

// Overpass respondio HTTP 200 pero con un error de ejecucion en "remark".
private class OverpassRuntimeException(message: String) : Exception(message)

// Primero consulta Overpass. Cada respuesta se guarda en Room; si luego falla la red, se
// muestran los últimos puntos guardados cerca de la ubicación (uso sin conexión).
class CollectionPointRepositoryImpl @Inject constructor(
    private val api: OverpassApi,
    private val dao: CachedCollectionPointDao,
    private val dispatcherProvider: DispatcherProvider
) : CollectionPointRepository {

    override suspend fun getNearbyPoints(
        center: GeoPoint,
        radiusMeters: Int
    ): Resource<List<CollectionPoint>> = withContext(dispatcherProvider.io) {
        try {
            val response = fetchFromAnyServer(OverpassApi.buildQuery(center, radiusMeters))
            val points = response.elements.orEmpty()
                .mapNotNull { it.toDomain(center) }
                .sortedBy { it.distanceMeters }
            Log.d(TAG, "Centro=$center, elementos=${response.elements?.size}, puntos validos=${points.size}")

            val now = System.currentTimeMillis()
            dao.insertAll(points.map { it.toEntity(cachedAt = now) })
            dao.deleteOlderThan(now - CACHE_TTL_MILLIS)
            Resource.Success(points)
        } catch (e: CancellationException) {
            throw e
        } catch (e: OverpassRuntimeException) {
            cachedOr(center, radiusMeters) {
                Resource.Error("El servicio de puntos de acopio no respondió correctamente.", ErrorType.SERVER_ERROR)
            }
        } catch (e: SocketTimeoutException) {
            cachedOr(center, radiusMeters) {
                Resource.Error("La búsqueda tardó demasiado. Intenta de nuevo.", ErrorType.TIMEOUT)
            }
        } catch (e: UnknownHostException) {
            cachedOr(center, radiusMeters) {
                Resource.Error("Sin conexión a internet.", ErrorType.NO_CONNECTION)
            }
        } catch (e: IOException) {
            cachedOr(center, radiusMeters) {
                Resource.Error("Sin conexión a internet.", ErrorType.NO_CONNECTION)
            }
        } catch (e: HttpException) {
            val message = if (e.code() == HTTP_TOO_MANY_REQUESTS) {
                "El servicio de mapas está saturado. Inténtalo en unos minutos."
            } else {
                "El servicio de puntos de acopio no respondió correctamente."
            }
            cachedOr(center, radiusMeters) { Resource.Error(message, ErrorType.SERVER_ERROR) }
        } catch (e: JsonParseException) {
            // Overpass a veces responde con una página de error en lugar de JSON.
            cachedOr(center, radiusMeters) {
                Resource.Error("El servicio de puntos de acopio respondió con datos inválidos.", ErrorType.SERVER_ERROR)
            }
        }
    }

    // Prueba cada servidor de Overpass en orden y devuelve la primera respuesta valida.
    // Se pasa al siguiente ante timeout, error HTTP (429/5xx), JSON invalido o un "remark" de
    // error con HTTP 200. Si todos fallan, se relanza el ultimo error.
    private suspend fun fetchFromAnyServer(query: String): OverpassResponseDto {
        var lastError: Exception? = null
        for (endpoint in OverpassApi.ENDPOINTS) {
            try {
                val response = api.getCollectionPoints(endpoint, query)
                val remark = response.remark
                Log.d(TAG, "Respuesta de $endpoint: elementos=${response.elements?.size}, remark=$remark")
                if (response.elements.isNullOrEmpty() && remark?.contains("error", ignoreCase = true) == true) {
                    lastError = OverpassRuntimeException(remark)
                    continue
                }
                return response
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                Log.w(TAG, "Fallo $endpoint: ${e.javaClass.simpleName}: ${e.message}")
                lastError = e
            } catch (e: HttpException) {
                Log.w(TAG, "Fallo $endpoint: HTTP ${e.code()} ${e.message()}")
                lastError = e
            } catch (e: JsonParseException) {
                Log.w(TAG, "Fallo $endpoint: JSON invalido: ${e.message}")
                lastError = e
            }
        }
        throw lastError ?: OverpassRuntimeException("Sin servidores disponibles")
    }

    // Si la red falla pero hay puntos guardados cerca, se devuelven marcados como desfasados
    // antes que propagar el error.
    private suspend fun cachedOr(
        center: GeoPoint,
        radiusMeters: Int,
        onMissing: () -> Resource.Error
    ): Resource<List<CollectionPoint>> {
        val cached = readCache(center, radiusMeters)
        return if (cached.isNotEmpty()) Resource.Success(cached, isStale = true) else onMissing()
    }

    private suspend fun readCache(center: GeoPoint, radiusMeters: Int): List<CollectionPoint> {
        val latDelta = radiusMeters / METERS_PER_DEGREE
        val lonScale = cos(center.latitude * PI / 180.0).coerceAtLeast(0.01)
        val lonDelta = radiusMeters / (METERS_PER_DEGREE * lonScale)
        return dao.getInBounds(
            minLat = center.latitude - latDelta,
            maxLat = center.latitude + latDelta,
            minLon = center.longitude - lonDelta,
            maxLon = center.longitude + lonDelta
        )
            .map { it.toDomain(center) }
            .filter { it.distanceMeters <= radiusMeters }
            .sortedBy { it.distanceMeters }
    }
}