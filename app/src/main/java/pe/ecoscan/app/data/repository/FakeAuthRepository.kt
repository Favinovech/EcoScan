package pe.ecoscan.app.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.ecoscan.app.domain.model.User
import pe.ecoscan.app.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAuthRepository @Inject constructor() : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: Flow<User?> = _currentUser.asStateFlow()

    override suspend fun signInWithEmail(email: String, pass: String): Result<User> {
        delay(1200) // Simula la latencia de red
        if (email.isBlank() || pass.isBlank()) {
            return Result.failure(IllegalArgumentException("Los campos no pueden estar vacíos"))
        }
        val user = User(
            uid = "fake-user-123",
            email = email,
            isEmailVerified = true
        )
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun signUpWithEmail(email: String, pass: String): Result<User> {
        delay(1200)
        if (email.isBlank() || pass.isBlank()) {
            return Result.failure(IllegalArgumentException("Los campos no pueden estar vacíos"))
        }
        val user = User(
            uid = "fake-user-new",
            email = email,
            isEmailVerified = true
        )
        _currentUser.value = user
        return Result.success(user)
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        delay(800)
        return if (email.isBlank()) {
            Result.failure(IllegalArgumentException("Ingresa un correo válido"))
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun signOut() {
        _currentUser.value = null
    }

    override suspend fun reauthenticate(password: String): Result<Unit> {
        delay(500)
        return if (password.isBlank()) {
            Result.failure(IllegalArgumentException("Ingresa tu contraseña"))
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        delay(500)
        _currentUser.value = null
        return Result.success(Unit)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        delay(1200)
        return if (idToken.isBlank()) {
            Result.failure(IllegalArgumentException("El token de Google no puede estar vacío"))
        } else {
            val user = User(
                uid = "google-user-123",
                email = "usuario.google@ecoscan.pe",
                isEmailVerified = true
            )
            _currentUser.value = user
            Result.success(user)
        }
    }
}