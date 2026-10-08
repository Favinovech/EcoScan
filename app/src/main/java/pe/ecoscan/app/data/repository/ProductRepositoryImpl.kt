package pe.ecoscan.app.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.core.common.ErrorType
import pe.ecoscan.app.core.common.Resource
import pe.ecoscan.app.data.local.dao.CachedProductDao
import pe.ecoscan.app.data.local.entity.CachedProductEntity
import pe.ecoscan.app.data.mapper.toDomain
import pe.ecoscan.app.data.mapper.toEntity
import pe.ecoscan.app.data.remote.api.EcoScanApi
import pe.ecoscan.app.domain.model.PackagedProduct
import pe.ecoscan.app.domain.repository.ProductRepository
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private val CACHE_TTL_MILLIS = TimeUnit.DAYS.toMillis(7)

class ProductRepositoryImpl @Inject constructor(
    private val api: EcoScanApi,
    private val dao: CachedProductDao,
    private val dispatcherProvider: DispatcherProvider
) : ProductRepository {

    override suspend fun getProductByBarcode(barcode: String): Resource<PackagedProduct> =
        withContext(dispatcherProvider.io) {
            val cached = dao.getByBarcode(barcode)
            val now = System.currentTimeMillis()

            if (cached != null && now - cached.cachedAt < CACHE_TTL_MILLIS) {
                return@withContext Resource.Success(cached.toDomain())
            }

            try {
                val response = api.getProductByBarcode(barcode, EcoScanApi.PRODUCT_FIELDS)
                if (response.status != 1 || response.product == null) {
                    return@withContext Resource.Error(
                        "No se encontró un producto con el código $barcode.",
                        ErrorType.NOT_FOUND
                    )
                }

                val product = response.product.toDomain(barcode)
                dao.insert(product.toEntity(cachedAt = now))
                Resource.Success(product)
            } catch (e: CancellationException) {
                throw e
            } catch (e: SocketTimeoutException) {
                cached.toStaleResourceOr {
                    Resource.Error("La consulta tardó demasiado. Intenta de nuevo.", ErrorType.TIMEOUT)
                }
            } catch (e: UnknownHostException) {
                cached.toStaleResourceOr {
                    Resource.Error("Sin conexión a internet.", ErrorType.NO_CONNECTION)
                }
            } catch (e: IOException) {
                cached.toStaleResourceOr {
                    Resource.Error("Sin conexión a internet.", ErrorType.NO_CONNECTION)
                }
            } catch (e: HttpException) {
                cached.toStaleResourceOr {
                    Resource.Error(
                        "El servidor de Open Food Facts no respondió correctamente.",
                        ErrorType.SERVER_ERROR
                    )
                }
            }
        }

    // Si la red falla pero hay un caché vencido disponible, se devuelve marcado como
    // desfasado antes que propagar el error.
    private inline fun CachedProductEntity?.toStaleResourceOr(
        onMissing: () -> Resource.Error
    ): Resource<PackagedProduct> =
        this?.let { Resource.Success(it.toDomain(), isStale = true) } ?: onMissing()
}
