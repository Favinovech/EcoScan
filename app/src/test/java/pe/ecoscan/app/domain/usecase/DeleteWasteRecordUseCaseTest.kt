package pe.ecoscan.app.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.ecoscan.app.domain.repository.WasteRecordRepository

class DeleteWasteRecordUseCaseTest {

    @Test
    fun `invoke delega la eliminacion en el repositorio`() = runTest {
        val repository = mockk<WasteRecordRepository>()
        coEvery { repository.deleteWasteRecord("7") } returns Result.success(Unit)

        val result = DeleteWasteRecordUseCase(repository)("7")

        assertTrue(result.isSuccess)
        coVerify { repository.deleteWasteRecord("7") }
    }
}