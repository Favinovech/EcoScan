package pe.ecoscan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.ecoscan.app.domain.model.User

interface AuthRepository {
    val currentUser: Flow<User?>

    suspend fun signInWithEmail(email: String, pass: String): Result<User>
    suspend fun signUpWithEmail(email: String, pass: String): Result<User>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun signOut()
}