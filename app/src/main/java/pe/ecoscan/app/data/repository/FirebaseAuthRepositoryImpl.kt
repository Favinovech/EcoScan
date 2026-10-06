package pe.ecoscan.app.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import pe.ecoscan.app.domain.model.User
import pe.ecoscan.app.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.auth.GoogleAuthProvider
@Singleton
class FirebaseAuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toDomainUser())
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun signInWithEmail(email: String, pass: String): Result<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                Result.success(firebaseUser.toDomainUser())
            } else {
                Result.failure(Exception("No se pudo obtener el usuario de Firebase"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUpWithEmail(email: String, pass: String): Result<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                Result.success(firebaseUser.toDomainUser())
            } else {
                Result.failure(Exception("Error al registrar usuario en Firebase"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun reauthenticate(password: String): Result<Unit> {
        val user = firebaseAuth.currentUser
            ?: return Result.failure(IllegalStateException("No hay una sesión activa"))
        val email = user.email
        if (email.isNullOrBlank()) {
            return Result.failure(IllegalStateException("La cuenta no tiene un correo asociado"))
        }
        return try {
            user.reauthenticate(EmailAuthProvider.getCredential(email, password)).await()
            Result.success(Unit)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Result.failure(Exception("Contraseña incorrecta"))
        } catch (e: FirebaseNetworkException) {
            Result.failure(Exception("Sin conexión a internet"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        val user = firebaseAuth.currentUser
            ?: return Result.failure(IllegalStateException("No hay una sesión activa"))
        return try {
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                Result.success(firebaseUser.toDomainUser())
            } else {
                Result.failure(Exception("Error al autenticar con Google en Firebase"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun FirebaseUser.toDomainUser(): User {
        return User(
            uid = this.uid,
            email = this.email ?: "",
            isEmailVerified = this.isEmailVerified
        )
    }
}