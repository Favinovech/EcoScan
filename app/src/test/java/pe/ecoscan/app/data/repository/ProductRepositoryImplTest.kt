package pe.ecoscan.app.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.core.common.ErrorType
import pe.ecoscan.app.core.common.Resource
import pe.ecoscan.app.data.local.dao.CachedProductDao
import pe.ecoscan.app.data.local.entity.CachedProductEntity
import pe.ecoscan.app.data.remote.api.EcoScanApi
import pe.ecoscan.app.data.remote.dto.ProductDto
import pe.ecoscan.app.data.remote.dto.ProductResponseDto
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class ProductRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeDispatcherProvider = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }

    private val api = mockk<EcoScanApi>()
    private val dao = mockk<CachedProductDao>()
    private val repository = ProductRepositoryImpl(api, dao, fakeDispatcherProvider)

    private val barcode = "7751271014013"
    private val sevenDaysMillis = TimeUnit.DAYS.toMillis(7)

    private fun cachedEntity(cachedAt: Long) = CachedProductEntity(
        barcode = barcode,
        name = "Agua Mineral",
        brand = "San Luis",
        packagingMaterials = "Plástico",
        imageUrl = null,
        disposalHint = "Enjuaga el envase y sepáralo como plástico.",
        cachedAt = cachedAt
    )

    @Test
    fun `con cache vigente no llama a la red`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        coEvery { dao.getByBarcode(barcode) } returns cachedEntity(cachedAt = now - 1_000L)

        val result = repository.getProductByBarcode(barcode)

        assertTrue(result is Resource.Success)
        coVerify(exactly = 0) { api.getProductByBarcode(any(), any()) }
    }

    @Test
    fun `con cache vencido consulta la red y actualiza el cache`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        coEvery { dao.getByBarcode(barcode) } returns cachedEntity(cachedAt = now - sevenDaysMillis - 1_000L)
        coEvery { api.getProductByBarcode(barcode, EcoScanApi.PRODUCT_FIELDS) } returns ProductResponseDto(
            status = 1,
            code = barcode,
            product = ProductDto(productName = "Agua Mineral", packaging = "Botella de plástico")
        )
        coEvery { dao.insert(any()) } returns Unit

        val result = repository.getProductByBarcode(barcode)

        assertTrue(result is Resource.Success)
        coVerify { api.getProductByBarcode(barcode, EcoScanApi.PRODUCT_FIELDS) }
        coVerify { dao.insert(any()) }
    }

    @Test
    fun `sin cache consulta la red`() = runTest(testDispatcher) {
        coEvery { dao.getByBarcode(barcode) } returns null
        coEvery { api.getProductByBarcode(barcode, EcoScanApi.PRODUCT_FIELDS) } returns ProductResponseDto(
            status = 1,
            code = barcode,
            product = ProductDto(productName = "Agua Mineral")
        )
        coEvery { dao.insert(any()) } returns Unit

        val result = repository.getProductByBarcode(barcode)

        assertTrue(result is Resource.Success)
        coVerify { api.getProductByBarcode(barcode, EcoScanApi.PRODUCT_FIELDS) }
    }

    @Test
    fun `producto no encontrado devuelve error NOT_FOUND`() = runTest(testDispatcher) {
        coEvery { dao.getByBarcode(barcode) } returns null
        coEvery { api.getProductByBarcode(barcode, EcoScanApi.PRODUCT_FIELDS) } returns ProductResponseDto(
            status = 0,
            code = barcode,
            product = null
        )

        val result = repository.getProductByBarcode(barcode) as Resource.Error

        assertEquals(ErrorType.NOT_FOUND, result.type)
    }

    @Test
    fun `timeout sin cache devuelve error TIMEOUT`() = runTest(testDispatcher) {
        coEvery { dao.getByBarcode(barcode) } returns null
        coEvery { api.getProductByBarcode(barcode, EcoScanApi.PRODUCT_FIELDS) } throws SocketTimeoutException()

        val result = repository.getProductByBarcode(barcode) as Resource.Error

        assertEquals(ErrorType.TIMEOUT, result.type)
    }

    @Test
    fun `sin conexion con cache vencido devuelve el cache marcado como desfasado`() = runTest(testDispatcher) {
        val now = System.currentTimeMillis()
        coEvery { dao.getByBarcode(barcode) } returns cachedEntity(cachedAt = now - sevenDaysMillis - 1_000L)
        coEvery { api.getProductByBarcode(barcode, EcoScanApi.PRODUCT_FIELDS) } throws IOException()

        val result = repository.getProductByBarcode(barcode) as Resource.Success

        assertTrue(result.isStale)
    }

    @Test
    fun `sin conexion sin cache devuelve error NO_CONNECTION`() = runTest(testDispatcher) {
        coEvery { dao.getByBarcode(barcode) } returns null
        coEvery { api.getProductByBarcode(barcode, EcoScanApi.PRODUCT_FIELDS) } throws IOException()

        val result = repository.getProductByBarcode(barcode) as Resource.Error

        assertEquals(ErrorType.NO_CONNECTION, result.type)
    }

    @Test
    fun `error de servidor sin cache devuelve error SERVER_ERROR`() = runTest(testDispatcher) {
        coEvery { dao.getByBarcode(barcode) } returns null
        coEvery { api.getProductByBarcode(barcode, EcoScanApi.PRODUCT_FIELDS) } throws HttpException(
            Response.error<ProductResponseDto>(500, "".toResponseBody(null))
        )

        val result = repository.getProductByBarcode(barcode) as Resource.Error

        assertEquals(ErrorType.SERVER_ERROR, result.type)
    }
}
