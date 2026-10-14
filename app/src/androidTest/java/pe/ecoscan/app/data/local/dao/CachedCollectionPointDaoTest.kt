package pe.ecoscan.app.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pe.ecoscan.app.data.local.EcoScanDatabase
import pe.ecoscan.app.data.local.entity.CachedCollectionPointEntity

@RunWith(AndroidJUnit4::class)
class CachedCollectionPointDaoTest {

    private lateinit var database: EcoScanDatabase
    private lateinit var dao: CachedCollectionPointDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, EcoScanDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.cachedCollectionPointDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun entity(id: String, lat: Double, lon: Double, cachedAt: Long = 1_000L) =
        CachedCollectionPointEntity(
            id = id, name = id, latitude = lat, longitude = lon,
            address = null, openingHours = null, acceptedMaterials = "PLASTIC|GLASS", cachedAt = cachedAt
        )

    @Test
    fun getInBoundsSoloDevuelveLosPuntosDentroDelRectangulo() = runBlocking {
        dao.insertAll(
            listOf(
                entity("dentro", lat = -12.10, lon = -77.00),
                entity("fuera-norte", lat = -11.90, lon = -77.00),
                entity("fuera-este", lat = -12.10, lon = -76.80)
            )
        )

        val result = dao.getInBounds(minLat = -12.20, maxLat = -12.00, minLon = -77.10, maxLon = -76.90)

        assertEquals(listOf("dentro"), result.map { it.id })
    }

    @Test
    fun insertarUnPuntoConElMismoIdLoReemplaza() = runBlocking {
        dao.insertAll(listOf(entity("a", -12.1, -77.0)))
        dao.insertAll(listOf(entity("a", -12.1, -77.0).copy(name = "Actualizado")))

        val result = dao.getInBounds(-13.0, -11.0, -78.0, -76.0)

        assertEquals(1, result.size)
        assertEquals("Actualizado", result.first().name)
    }

    @Test
    fun deleteOlderThanBorraSoloLosPuntosAntiguos() = runBlocking {
        dao.insertAll(
            listOf(
                entity("viejo", -12.1, -77.0, cachedAt = 1_000L),
                entity("reciente", -12.1, -77.0, cachedAt = 9_000L)
            )
        )

        dao.deleteOlderThan(5_000L)

        val result = dao.getInBounds(-13.0, -11.0, -78.0, -76.0)
        assertEquals(listOf("reciente"), result.map { it.id })
    }
}