package pe.ecoscan.app.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pe.ecoscan.app.data.local.EcoScanDatabase
import pe.ecoscan.app.data.local.entity.WasteRecordEntity
import pe.ecoscan.app.domain.model.WasteCategory

@RunWith(AndroidJUnit4::class)
class WasteRecordDaoTest {

    private lateinit var database: EcoScanDatabase
    private lateinit var dao: WasteRecordDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, EcoScanDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.wasteRecordDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertarYLeerDevuelveLosRegistrosEsperados() = runBlocking {
        val record = WasteRecordEntity(
            category = WasteCategory.PLASTIC,
            weightKg = 2.5,
            points = 25,
            recordedAt = 1_000L
        )

        dao.insert(record)

        val records = dao.getAll().first()
        assertEquals(1, records.size)
        assertEquals(WasteCategory.PLASTIC, records.first().category)
        assertEquals(2.5, records.first().weightKg, 0.0)
        assertEquals(25, records.first().points)
    }

    @Test
    fun getTotalPointsSumaCorrectamente() = runBlocking {
        dao.insertAll(
            listOf(
                WasteRecordEntity(category = WasteCategory.PLASTIC, weightKg = 2.5, points = 25, recordedAt = 1_000L),
                WasteRecordEntity(category = WasteCategory.GLASS, weightKg = 5.0, points = 30, recordedAt = 2_000L)
            )
        )

        val totalPoints = dao.getTotalPoints().first()

        assertEquals(55, totalPoints)
    }

    @Test
    fun getTotalPointsDevuelveCeroCuandoLaTablaEstaVacia() = runBlocking {
        val totalPoints = dao.getTotalPoints().first()

        assertEquals(0, totalPoints)
    }
}
