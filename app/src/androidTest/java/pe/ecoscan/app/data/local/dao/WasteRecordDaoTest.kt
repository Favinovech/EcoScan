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

    // ---- HU06: historial por usuario y sincronización ----

    @Test
    fun observeByUserSoloDevuelveRegistrosDelUsuarioSinLosEliminados() = runBlocking {
        dao.insertAll(
            listOf(
                WasteRecordEntity(category = WasteCategory.PLASTIC, weightKg = 1.0, points = 15, recordedAt = 1_000L, userId = "ana", syncId = "a1"),
                WasteRecordEntity(category = WasteCategory.GLASS, weightKg = 2.0, points = 40, recordedAt = 2_000L, userId = "ana", syncId = "a2", isDeleted = true),
                WasteRecordEntity(category = WasteCategory.PAPER, weightKg = 3.0, points = 30, recordedAt = 3_000L, userId = "luis", syncId = "l1")
            )
        )

        val records = dao.observeByUser("ana").first()

        assertEquals(1, records.size)
        assertEquals(WasteCategory.PLASTIC, records.first().category)
    }

    @Test
    fun claimLegacyRecordsAsignaLosRegistrosSinDuenoAlUsuario() = runBlocking {
        dao.insert(WasteRecordEntity(category = WasteCategory.METAL, weightKg = 1.0, points = 25, recordedAt = 1_000L))

        dao.claimLegacyRecords("ana")

        assertEquals(1, dao.observeByUser("ana").first().size)
    }

    @Test
    fun editarMarcaElRegistroComoPendienteDeSincronizar() = runBlocking {
        val id = dao.insert(
            WasteRecordEntity(category = WasteCategory.PLASTIC, weightKg = 1.0, points = 15, recordedAt = 1_000L, userId = "ana", syncId = "a1", isSynced = true)
        )

        dao.updateEditable(id, WasteCategory.GLASS, 2.0, 40, updatedAt = 5_000L)

        val updated = dao.getAllByUser("ana").first()
        assertEquals(WasteCategory.GLASS, updated.category)
        assertEquals(2.0, updated.weightKg, 0.0)
        assertEquals(40, updated.points)
        assertEquals(false, updated.isSynced)
        assertEquals(1, dao.getPendingByUser("ana").size)
    }

    @Test
    fun eliminarOcultaElRegistroPeroLoDejaPendienteParaSincronizar() = runBlocking {
        val id = dao.insert(
            WasteRecordEntity(category = WasteCategory.PLASTIC, weightKg = 1.0, points = 15, recordedAt = 1_000L, userId = "ana", syncId = "a1", isSynced = true)
        )

        dao.markDeleted(id, updatedAt = 5_000L)

        assertEquals(0, dao.observeByUser("ana").first().size)
        assertEquals(1, dao.getPendingByUser("ana").size)
    }

    @Test
    fun markSyncedIfUnchangedNoMarcaSiElRegistroCambioMientrasSeSubia() = runBlocking {
        val id = dao.insert(
            WasteRecordEntity(category = WasteCategory.PLASTIC, weightKg = 1.0, points = 15, recordedAt = 1_000L, userId = "ana", syncId = "a1", updatedAt = 2_000L)
        )

        // Se subió la versión con updatedAt = 1_000, pero ya se editó (updatedAt = 2_000).
        dao.markSyncedIfUnchanged(id, updatedAt = 1_000L)

        assertEquals(1, dao.getPendingByUser("ana").size)
    }
}