package pe.ecoscan.app.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import pe.ecoscan.app.domain.model.User
import pe.ecoscan.app.domain.model.UserProfile
import pe.ecoscan.app.domain.repository.AuthRepository
import pe.ecoscan.app.domain.repository.ProfileRepository
import pe.ecoscan.app.domain.sync.MAX_SYNC_ATTEMPTS
import pe.ecoscan.app.domain.sync.WasteRecordSyncer
import java.io.IOException

// SyncWorker tampoco usa Context en su propia lógica, pero TestListenableWorkerBuilder es la
// forma de levantarlo completo (con runAttemptCount real) sin pelear con el constructor de
// WorkerParameters a mano. WasteRecordSyncer es una interfaz (domain/sync) justamente para poder
// sustituirla aquí por un fake.
@RunWith(AndroidJUnit4::class)
class SyncWorkerTest {

    private val user = User(uid = "ana", email = "ana@ecoscan.pe")

    @Test
    fun sinSesionTerminaConExitoSinSincronizar() = runBlocking {
        val syncer = FakeWasteRecordSyncer(Result.success(Unit))
        val worker = buildWorker(
            authRepository = FakeAuthRepository(null),
            syncer = syncer,
            profileRepository = FakeProfileRepository(Result.success(Unit))
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(0, syncer.callCount)
    }

    @Test
    fun siAmbasSincronizacionesTienenExitoElResultadoEsExito() = runBlocking {
        val worker = buildWorker(
            authRepository = FakeAuthRepository(user),
            syncer = FakeWasteRecordSyncer(Result.success(Unit)),
            profileRepository = FakeProfileRepository(Result.success(Unit))
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun errorTransitorioEnElPrimerIntentoReintenta() = runBlocking {
        val worker = buildWorker(
            authRepository = FakeAuthRepository(user),
            syncer = FakeWasteRecordSyncer(Result.failure(IOException("Sin conexión"))),
            profileRepository = FakeProfileRepository(Result.success(Unit)),
            runAttemptCount = 0
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun errorTransitorioAlAgotarLosIntentosFalla() = runBlocking {
        val worker = buildWorker(
            authRepository = FakeAuthRepository(user),
            syncer = FakeWasteRecordSyncer(Result.failure(IOException("Sin conexión"))),
            profileRepository = FakeProfileRepository(Result.success(Unit)),
            runAttemptCount = MAX_SYNC_ATTEMPTS - 1
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
    }

    @Test
    fun errorPermanenteFallaAunEnElPrimerIntento() = runBlocking {
        val worker = buildWorker(
            authRepository = FakeAuthRepository(user),
            syncer = FakeWasteRecordSyncer(Result.failure(IllegalStateException("Dato inválido"))),
            profileRepository = FakeProfileRepository(Result.success(Unit)),
            runAttemptCount = 0
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.failure(), result)
    }

    private fun buildWorker(
        authRepository: AuthRepository,
        syncer: WasteRecordSyncer,
        profileRepository: ProfileRepository,
        runAttemptCount: Int = 0
    ): SyncWorker {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return TestListenableWorkerBuilder<SyncWorker>(context)
            .setRunAttemptCount(runAttemptCount)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ) = SyncWorker(appContext, workerParameters, authRepository, syncer, profileRepository)
            })
            .build()
    }

    private class FakeAuthRepository(user: User?) : AuthRepository {
        override val currentUser: Flow<User?> = MutableStateFlow(user).asStateFlow()
        override suspend fun signInWithEmail(email: String, pass: String) = error("no usado")
        override suspend fun signUpWithEmail(email: String, pass: String) = error("no usado")
        override suspend fun signInWithGoogle(idToken: String) = error("no usado")
        override suspend fun sendPasswordResetEmail(email: String) = error("no usado")
        override suspend fun signOut() = error("no usado")
        override suspend fun reauthenticate(password: String) = error("no usado")
        override suspend fun deleteAccount() = error("no usado")
    }

    private class FakeWasteRecordSyncer(private val result: Result<Unit>) : WasteRecordSyncer {
        var callCount = 0

        override suspend fun sync(uid: String): Result<Unit> {
            callCount++
            return result
        }
    }

    private class FakeProfileRepository(private val result: Result<Unit>) : ProfileRepository {
        override fun observeProfile(uid: String): Flow<UserProfile?> = error("no usado")
        override suspend fun refreshProfile(user: User) = error("no usado")
        override suspend fun saveProfile(profile: UserProfile, newPhotoUri: String?) = error("no usado")
        override suspend fun deleteProfile(uid: String) = error("no usado")
        override suspend fun syncPendingProfile(uid: String): Result<Unit> = result
    }
}
