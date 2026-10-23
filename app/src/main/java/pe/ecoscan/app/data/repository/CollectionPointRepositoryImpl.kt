package pe.ecoscan.app.data.repository

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
            val response = api.getCollectionPoints(OverpassApi.buildQuery(center, radiusMeters))
            val points = response.elements.orEmpty()
                .mapNotNull { it.toDomain(center) }
                .sortedBy { it.distanceMeters }

            val now = System.currentTimeMillis()
            dao.insertAll(points.map { it.toEntity(cachedAt = now) })
            dao.deleteOlderThan(now - CACHE_TTL_MILLIS)
            Resource.Success(points)
        } catch (e: CancellationException) {
            throw e
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