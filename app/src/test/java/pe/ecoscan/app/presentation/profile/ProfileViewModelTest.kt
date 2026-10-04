package pe.ecoscan.app.presentation.profile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.data.repository.FakeAuthRepository
import pe.ecoscan.app.data.repository.FakeProfileRepository
import pe.ecoscan.app.domain.model.UserProfile
import pe.ecoscan.app.domain.usecase.DeleteAccountUseCase
import pe.ecoscan.app.domain.usecase.ObserveUserProfileUseCase
import pe.ecoscan.app.domain.usecase.RefreshUserProfileUseCase
import pe.ecoscan.app.domain.usecase.SaveUserProfileUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val dispatcherProvider = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var profileRepository: FakeProfileRepository
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        profileRepository = FakeProfileRepository()
        viewModel = ProfileViewModel(
            authRepository = authRepository,
            observeUserProfileUseCase = ObserveUserProfileUseCase(profileRepository),
            refreshUserProfileUseCase = RefreshUserProfileUseCase(profileRepository),
            saveUserProfileUseCase = SaveUserProfileUseCase(profileRepository),
            deleteAccountUseCase = DeleteAccountUseCase(authRepository, profileRepository),
            dispatcherProvider = dispatcherProvider
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `el estado inicial no esta cargando y no tiene error`() {
        val uiState = viewModel.uiState.value

        assertFalse(uiState.isLoading)
        assertNull(uiState.errorMessage)
        assertEquals(ProfileUiState(), uiState)
    }

    @Test
    fun `al iniciar sesion carga el perfil guardado en el formulario`() = runTest(testDispatcher) {
        authRepository.signInWithEmail("ana@ecoscan.pe", "secreto")
        profileRepository.profileFlow.value = UserProfile(
            uid = "fake-user-123",
            email = "ana@ecoscan.pe",
            displayName = "Ana",
            district = "Miraflores",
            weeklyGoalKg = 12
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("ana@ecoscan.pe", state.email)
        assertEquals("Ana", state.displayName)
        assertEquals("Miraflores", state.district)
        assertEquals(12, state.weeklyGoalKg)
        assertFalse(state.hasUnsavedChanges)
    }

    @Test
    fun `editar el nombre marca cambios sin guardar`() = runTest(testDispatcher) {
        authRepository.signInWithEmail("ana@ecoscan.pe", "secreto")
        advanceUntilIdle()

        viewModel.onDisplayNameChange("Ana")

        assertTrue(viewModel.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `guardar sin nombre muestra error y no guarda`() = runTest(testDispatcher) {
        authRepository.signInWithEmail("ana@ecoscan.pe", "secreto")
        advanceUntilIdle()
        viewModel.onDistrictChange("Miraflores")

        viewModel.saveProfile()
        advanceUntilIdle()

        assertEquals("Ingresa tu nombre", viewModel.uiState.value.errorMessage)
        assertNull(profileRepository.lastSavedProfile)
    }

    @Test
    fun `guardar con datos validos actualiza el perfil y limpia los cambios`() = runTest(testDispatcher) {
        authRepository.signInWithEmail("ana@ecoscan.pe", "secreto")
        advanceUntilIdle()
        viewModel.onDisplayNameChange("Ana")
        viewModel.onDistrictChange("Miraflores")

        viewModel.saveProfile()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Perfil actualizado", state.infoMessage)
        assertFalse(state.hasUnsavedChanges)
        assertFalse(state.isSaving)
        assertEquals("Ana", profileRepository.lastSavedProfile?.displayName)
    }

    @Test
    fun `cerrar sesion marca isSignedOut`() = runTest(testDispatcher) {
        authRepository.signInWithEmail("ana@ecoscan.pe", "secreto")
        advanceUntilIdle()

        viewModel.signOut()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSignedOut)
    }
}