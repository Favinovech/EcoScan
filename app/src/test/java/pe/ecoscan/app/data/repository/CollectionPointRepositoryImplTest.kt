package pe.ecoscan.app.data.repository

import com.google.gson.JsonSyntaxException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.core.common.ErrorType
import pe.ecoscan.app.core.common.Resource
import pe.ecoscan.app.data.local.dao.CachedCollectionPointDao
import pe.ecoscan.app.data.local.entity.CachedCollectionPointEntity
import pe.ecoscan.app.data.remote.api.OverpassApi
import pe.ecoscan.app.data.remote.dto.OverpassElementDto
import pe.ecoscan.app.data.remote.dto.OverpassResponseDto
import pe.ecoscan.app.domain.model.CollectionPoint
import pe.ecoscan.app.domain.model.GeoPoint
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class CollectionPointRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeDispatcherProvider = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }

    private val api = mockk<OverpassApi>()
    private val dao = mockk<CachedCollectionPointDao>()
    private val repository = CollectionPointRepositoryImpl(api, dao, fakeDispatcherProvider)

    private val center = GeoPoint(-12.1, -77.0)
    private val radius = 5_000

    // ~1 km al este del centro y ~500 m al este.
    private val farElement = OverpassElementDto(type = "node", id = 1L, lat = -12.1, lon = -76.991, tags = mapOf("name" to "Lejos"))
    private val nearElement = OverpassElementDto(type = "node", id = 2L, lat = -12.1, lon = -76.9955, tags = mapOf("name" to "Cerca"))

    private fun cachedEntity(id: String, longitude: Double) = CachedCollectionPointEntity(
        id = id, name = "Guardado", latitude = -12.1, longitude = longitude,
        address = null, openingHours = null, acceptedMaterials = "PLASTIC", cachedAt = 1L
    )

    @Test
    fun `con red devuelve los puntos ordenados por distancia y los guarda en cache`() = runTest(testDispatcher) {
        coEvery { api.getCollectionPoints(any()) } returns
                OverpassResponseDto(elements = listOf(farElement, nearElement))
        coEvery { dao.insertAll(any()) } returns Unit
        coEvery { dao.deleteOlderThan(any()) } returns Unit

        val result = repository.getNearbyPoints(center, radius)

        val success = result as Resource.Success
        assertFalse(success.isStale)
        assertEquals(listOf("Cerca", "Lejos"), success.data.map { it.name })
        coVerify { dao.insertAll(match { it.size == 2 }) }
        coVerify { dao.deleteOlderThan(any()) }
    }

    @Test
    fun `los elementos sin coordenadas se ignoran`() = runTest(testDispatcher) {
        coEvery { api.getCollectionPoints(any()) } returns OverpassResponseDto(
            elements = listOf(nearElement, OverpassElementDto(type = "node", id = 9L))
        )
        coEvery { dao.insertAll(any()) } returns Unit
        coEvery { dao.deleteOlderThan(any()) } returns Unit

        val result = repository.getNearbyPoints(center, radius) as Resource.Success

        assertEquals(1, result.data.size)
    }

    @Test
    fun `la consulta enviada incluye el radio y la ubicacion`() = runTest(testDispatcher) {
        coEvery { api.getCollectionPoints(any()) } returns OverpassResponseDto(elements = emptyList())
        coEvery { dao.insertAll(any()) } returns Unit
        coEvery { dao.deleteOlderThan(any()) } returns Unit

        repository.getNearbyPoints(center, radius)

        coVerify { api.getCollectionPoints(match { it.contains("around:5000,-12.1,-77.0") }) }
    }

    @Test
    fun `sin red devuelve los puntos guardados marcados como desfasados`() = runTest(testDispatcher) {
        coEvery { api.getCollectionPoints(any()) } throws IOException()
        coEvery { dao.getInBounds(any(), any(), any(), any()) } returns
                listOf(cachedEntity("node/10", -76.9955))

        val result = repository.getNearbyPoints(center, radius)

        val success = result as Resource.Success
        assertTrue(success.isStale)
        assertEquals(listOf("node/10"), success.data.map { it.id })
    }

    @Test
    fun `de la cache solo se devuelven los puntos dentro del radio`() = runTest(testDispatcher) {
        coEvery { api.getCollectionPoints(any()) } throws IOException()
        // Cae dentro del rectángulo de búsqueda pero fuera del círculo de 5 km.
        coEvery { dao.getInBounds(any(), any(), any(), any()) } returns
                listOf(cachedEntity("lejos", -76.9500), cachedEntity("cerca", -76.9955))

        val result = repository.getNearbyPoints(center, radius) as Resource.Success

        assertEquals(listOf("cerca"), result.data.map { it.id })
    }

    @Test
    fun `sin red y sin cache devuelve error de conexion`() = runTest(testDispatcher) {
        coEvery { api.getCollectionPoints(any()) } throws IOException()
        coEvery { dao.getInBounds(any(), any(), any(), any()) } returns emptyList()

        val result = repository.getNearbyPoints(center, radius)

        assertEquals(ErrorType.NO_CONNECTION, (result as Resource.Error).type)
    }

    @Test
    fun `un tiempo de espera agotado sin cache devuelve error de timeout`() = runTest(testDispatcher) {
        coEvery { api.getCollectionPoints(any()) } throws SocketTimeoutException()
        coEvery { dao.getInBounds(any(), any(), any(), any()) } returns emptyList()

        val result = repository.getNearbyPoints(center, radius)

        assertEquals(ErrorType.TIMEOUT, (result as Resource.Error).type)
    }

    @Test
    fun `el codigo 429 se informa como servicio saturado`() = runTest(testDispatcher) {
        coEvery { api.getCollectionPoints(any()) } throws
                HttpException(Response.error<OverpassResponseDto>(429, "".toResponseBody(null)))
        coEvery { dao.getInBounds(any(), any(), any(), any()) } returns emptyList()

        val result = repository.getNearbyPoints(center, radius) as Resource.Error

        assertEquals(ErrorType.SERVER_ERROR, result.type)
        assertTrue(result.message.contains("saturado"))
    }

    @Test
    fun `una respuesta que no es JSON se trata como error del servidor`() = runTest(testDispatcher) {
        coEvery { api.getCollectionPoints(any()) } throws JsonSyntaxException("<html>")
        coEvery { dao.getInBounds(any(), any(), any(), any()) } returns emptyList()

        val result = repository.getNearbyPoints(center, radius)

        assertEquals(ErrorType.SERVER_ERROR, (result as Resource.Error).type)
    }

    @Test
    fun `un error del servidor con cache devuelve los puntos guardados`() = runTest(testDispatcher) {
        coEvery { api.getCollectionPoints(any()) } throws
                HttpException(Response.error<OverpassResponseDto>(504, "".toResponseBody(null)))
        coEvery { dao.getInBounds(any(), any(), any(), any()) } returns
                listOf(cachedEntity("node/10", -76.9955))

        val result = repository.getNearbyPoints(center, radius)

        assertTrue((result as Resource.Success).isStale)
    }
}