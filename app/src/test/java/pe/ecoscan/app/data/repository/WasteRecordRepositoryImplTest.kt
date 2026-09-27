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
import org.junit.Test
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.data.local.dao.WasteRecordDao
import pe.ecoscan.app.data.local.entity.WasteRecordEntity
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteRecord

@OptIn(ExperimentalCoroutinesApi::class)
class WasteRecordRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeDispatcherProvider = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }

    private val dao = mockk<WasteRecordDao>()
    private val repository = WasteRecordRepositoryImpl(dao, fakeDispatcherProvider)

    @Test
    fun `getWasteRecords mapea las entidades del DAO a modelos de dominio`() = runTest(testDispatcher) {
        val entity = WasteRecordEntity(id = 7L, category = WasteCategory.GLASS, weightKg = 5.0, points = 30, recordedAt = 1_000L)
        every { dao.getAll() } returns flowOf(listOf(entity))

        val result = repository.getWasteRecords().first()

        assertEquals(listOf(WasteRecord("7", WasteCategory.GLASS, 5.0, 30, 1_000L)), result)
    }

    @Test
    fun `insert delega en el DAO con la entidad mapeada`() = runTest(testDispatcher) {
        coEvery { dao.insert(any()) } returns 1L
        val record = WasteRecord("0", WasteCategory.PLASTIC, weightKg = 2.5, points = 25, recordedAt = 500L)

        repository.insert(record)

        coVerify { dao.insert(WasteRecordEntity(id = 0L, category = WasteCategory.PLASTIC, weightKg = 2.5, points = 25, recordedAt = 500L)) }
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
