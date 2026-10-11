package pe.ecoscan.app.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.data.local.dao.WasteRecordDao
import pe.ecoscan.app.data.local.entity.WasteRecordEntity
import pe.ecoscan.app.domain.model.User
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteRecord
import pe.ecoscan.app.domain.repository.AuthRepository

@OptIn(ExperimentalCoroutinesApi::class)
class WasteRecordRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeDispatcherProvider = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }

    private val dao = mockk<WasteRecordDao>()
    private val authRepository = mockk<AuthRepository>()
    private val repository = WasteRecordRepositoryImpl(dao, authRepository, fakeDispatcherProvider)

    private fun signedInAs(uid: String?) {
        every { authRepository.currentUser } returns flowOf(uid?.let { User(it, "ana@ecoscan.pe", true) })
    }

    @Test
    fun `getWasteRecords mapea los registros del usuario a modelos de dominio`() = runTest(testDispatcher) {
        signedInAs("uid-1")
        val entity = WasteRecordEntity(
            id = 7L, category = WasteCategory.GLASS, weightKg = 5.0, points = 30,
            recordedAt = 1_000L, isSynced = true, userId = "uid-1"
        )
        coEvery { dao.claimLegacyRecords("uid-1") } returns Unit
        every { dao.observeByUser("uid-1") } returns flowOf(listOf(entity))

        val result = repository.getWasteRecords().first()

        assertEquals(
            listOf(WasteRecord("7", WasteCategory.GLASS, 5.0, 30, 1_000L, isSynced = true)),
            result
        )
        coVerify { dao.claimLegacyRecords("uid-1") }
    }

    @Test
    fun `getWasteRecords sin sesion devuelve lista vacia`() = runTest(testDispatcher) {
        signedInAs(null)

        val result = repository.getWasteRecords().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `insert guarda el registro con el usuario, un syncId y pendiente de sincronizar`() = runTest(testDispatcher) {
        signedInAs("uid-1")
        coEvery { dao.insert(any()) } returns 1L
        val record = WasteRecord("0", WasteCategory.PLASTIC, weightKg = 2.5, points = 25, recordedAt = 500L)

        repository.insert(record)

        coVerify {
            dao.insert(
                match {
                    it.userId == "uid-1" &&
                            it.syncId.isNotEmpty() &&
                            !it.isSynced &&
                            !it.isDeleted &&
                            it.category == WasteCategory.PLASTIC &&
                            it.weightKg == 2.5 &&
                            it.points == 25 &&
                            it.recordedAt == 500L
                }
            )
        }
    }

    @Test
    fun `insert sin sesion no guarda nada`() = runTest(testDispatcher) {
        signedInAs(null)

        repository.insert(WasteRecord("0", WasteCategory.PLASTIC, 1.0, 10, 1L))

        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun `updateWasteRecord actualiza los campos editables en el DAO`() = runTest(testDispatcher) {
        coEvery { dao.updateEditable(any(), any(), any(), any(), any()) } returns 1
        val record = WasteRecord("7", WasteCategory.METAL, 2.0, 50, 1_000L)

        val result = repository.updateWasteRecord(record)

        assertTrue(result.isSuccess)
        coVerify { dao.updateEditable(7L, WasteCategory.METAL, 2.0, 50, any()) }
    }

    @Test
    fun `updateWasteRecord falla si el registro ya no existe`() = runTest(testDispatcher) {
        coEvery { dao.updateEditable(any(), any(), any(), any(), any()) } returns 0

        val result = repository.updateWasteRecord(WasteRecord("7", WasteCategory.METAL, 2.0, 50, 1_000L))

        assertTrue(result.isFailure)
    }

    @Test
    fun `deleteWasteRecord marca el registro como eliminado sin borrar la fila`() = runTest(testDispatcher) {
        coEvery { dao.markDeleted(7L, any()) } returns 1

        val result = repository.deleteWasteRecord("7")

        assertTrue(result.isSuccess)
        coVerify { dao.markDeleted(7L, any()) }
        coVerify(exactly = 0) { dao.deleteById(any()) }
    }

    @Test
    fun `deleteWasteRecord con id no numerico devuelve error`() = runTest(testDispatcher) {
        val result = repository.deleteWasteRecord("no-es-un-id")

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { dao.markDeleted(any(), any()) }
    }

    @Test
    fun `deleteById con id numerico delega en el DAO`() = runTest(testDispatcher) {
        coEvery { dao.deleteById(7L) } returns Unit

        repository.deleteById("7")

        coVerify { dao.deleteById(7L) }
    }

    @Test
    fun `deleteById con id no numerico no llama al DAO`() = runTest(testDispatcher) {
        repository.deleteById("no-es-un-id")

        coVerify(exactly = 0) { dao.deleteById(any()) }
    }

    @Test
    fun `deleteAll delega en el DAO`() = runTest(testDispatcher) {
        coEvery { dao.deleteAll() } returns Unit

        repository.deleteAll()

        coVerify { dao.deleteAll() }
    }
}