package pe.ecoscan.app.presentation.auth

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.data.repository.FakeAuthRepository

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeDispatcherProvider = object : DispatcherProvider {
        override val main = testDispatcher
        override val io = testDispatcher
        override val default = testDispatcher
    }

    private lateinit var fakeRepository: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeAuthRepository()
        viewModel = AuthViewModel(
            authRepository = fakeRepository,
            dispatcherProvider = fakeDispatcherProvider
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial no esta cargando y no tiene errores`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals(null, state.errorMessage)
        assertEquals(null, state.infoMessage)
    }

    @Test
    fun `signIn con campos validos actualiza uiState a success`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertFalse(initial.isLoading)

            viewModel.signIn("test@ecoscan.pe", "password123")

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            testDispatcher.scheduler.advanceUntilIdle()

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertTrue(successState.isSuccess)
            assertEquals("test@ecoscan.pe", successState.user?.email)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signIn con campos vacios emite mensaje de error`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertFalse(initial.isLoading)

            viewModel.signIn("", "")

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            testDispatcher.scheduler.advanceUntilIdle()

            val errorState = awaitItem()
            assertFalse(errorState.isLoading)
            assertFalse(errorState.isSuccess)
            assertEquals("Los campos no pueden estar vacíos", errorState.errorMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `signUp con datos validos completa registro exitosamente`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertFalse(initial.isLoading)

            viewModel.signUp("nuevo@ecoscan.pe", "password123")

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            testDispatcher.scheduler.advanceUntilIdle()

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertTrue(successState.isSuccess)
            assertEquals("nuevo@ecoscan.pe", successState.user?.email)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `sendPasswordReset con correo emite infoMessage`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertFalse(initial.isLoading)

            viewModel.sendPasswordReset("recuperar@ecoscan.pe")

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            testDispatcher.scheduler.advanceUntilIdle()

            val successState = awaitItem()
            assertFalse(successState.isLoading)
            assertEquals("Correo de recuperación enviado con éxito", successState.infoMessage)

            cancelAndIgnoreRemainingEvents()
        }
    }
}