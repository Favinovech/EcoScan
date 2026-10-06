package pe.ecoscan.app.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.ecoscan.app.data.repository.FakeProfileRepository
import pe.ecoscan.app.domain.model.UserProfile

class SaveUserProfileUseCaseTest {

    private val repository = FakeProfileRepository()
    private val useCase = SaveUserProfileUseCase(repository)

    private fun profile(
        name: String = "Ana",
        district: String = "Miraflores",
        goal: Int = 5
    ) = UserProfile(
        uid = "uid-1",
        email = "ana@ecoscan.pe",
        displayName = name,
        district = district,
        weeklyGoalKg = goal
    )

    @Test
    fun `nombre vacio devuelve error y no guarda`() = runTest {
        val result = useCase(profile(name = "   "), null)

        assertTrue(result.isFailure)
        assertEquals("Ingresa tu nombre", result.exceptionOrNull()?.message)
        assertNull(repository.lastSavedProfile)
    }

    @Test
    fun `distrito vacio devuelve error`() = runTest {
        val result = useCase(profile(district = ""), null)

        assertTrue(result.isFailure)
        assertEquals("Ingresa tu distrito", result.exceptionOrNull()?.message)
    }

    @Test
    fun `meta semanal fuera de rango devuelve error`() = runTest {
        val result = useCase(profile(goal = 0), null)

        assertTrue(result.isFailure)
    }

    @Test
    fun `datos validos se guardan sin espacios sobrantes`() = runTest {
        val result = useCase(profile(name = "  Ana  ", district = " Miraflores "), "content://foto")

        assertTrue(result.isSuccess)
        assertEquals("Ana", repository.lastSavedProfile?.displayName)
        assertEquals("Miraflores", repository.lastSavedProfile?.district)
        assertEquals("content://foto", repository.lastSavedPhotoUri)
    }
}