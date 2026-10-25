package pe.ecoscan.app.data.reminder

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
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import pe.ecoscan.app.data.repository.CollectionScheduleRepositoryImpl
import pe.ecoscan.app.domain.model.CollectionSchedule
import pe.ecoscan.app.domain.model.User
import pe.ecoscan.app.domain.model.UserProfile
import pe.ecoscan.app.domain.notification.ReminderNotifier
import pe.ecoscan.app.domain.repository.AuthRepository
import pe.ecoscan.app.domain.repository.CollectionScheduleRepository
import pe.ecoscan.app.domain.repository.ProfileRepository

// ReminderWorker no usa Context en su propia lógica (lo delega todo en sus dependencias), pero
// sí lo necesita el CoroutineWorker del que hereda, así que TestListenableWorkerBuilder
// (work-testing) es la forma correcta de construirlo completo, con runAttemptCount y Result
// reales, en vez de llamar al constructor a mano.
@RunWith(AndroidJUnit4::class)
class ReminderWorkerTest {

    private val collectionScheduleRepository: CollectionScheduleRepository = CollectionScheduleRepositoryImpl()

    @Test
    fun sinSesionTerminaConExitoSinNotificar() = runBlocking {
        val notifier = FakeReminderNotifier()
        val worker = buildWorker(
            authRepository = FakeAuthRepository(user = null),
            profileRepository = FakeProfileRepository(profile = null),
            notifier = notifier
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertNull(notifier.lastSchedule)
    }

    @Test
    fun sinDistritoEnElPerfilNoNotifica() = runBlocking {
        val notifier = FakeReminderNotifier()
        val profile = UserProfile(uid = "ana", email = "ana@ecoscan.pe", district = "")
        val worker = buildWorker(
            authRepository = FakeAuthRepository(user = User(uid = "ana", email = "ana@ecoscan.pe")),
            profileRepository = FakeProfileRepository(profile = profile),
            notifier = notifier
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertNull(notifier.lastSchedule)
    }

    @Test
    fun conDistritoNotificaConElHorarioDeEseDistrito() = runBlocking {
        val notifier = FakeReminderNotifier()
        val profile = UserProfile(uid = "ana", email = "ana@ecoscan.pe", district = "Surco")
        val worker = buildWorker(
            authRepository = FakeAuthRepository(user = User(uid = "ana", email = "ana@ecoscan.pe")),
            profileRepository = FakeProfileRepository(profile = profile),
            notifier = notifier
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertEquals(collectionScheduleRepository.getSchedule("Surco"), notifier.lastSchedule)
    }

    private fun buildWorker(
        authRepository: AuthRepository,
        profileRepository: ProfileRepository,
        notifier: ReminderNotifier
    ): ReminderWorker {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return TestListenableWorkerBuilder<ReminderWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ) = ReminderWorker(
                    appContext,
                    workerParameters,
                    authRepository,
                    profileRepository,
                    collectionScheduleRepository,
                    notifier
                )
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

    private class FakeProfileRepository(profile: UserProfile?) : ProfileRepository {
        private val flow = MutableStateFlow(profile)
        override fun observeProfile(uid: String): Flow<UserProfile?> = flow.asStateFlow()
        override suspend fun refreshProfile(user: User) = error("no usado")
        override suspend fun saveProfile(profile: UserProfile, newPhotoUri: String?) = error("no usado")
        override suspend fun deleteProfile(uid: String) = error("no usado")
        override suspend fun syncPendingProfile(uid: String) = error("no usado")
    }

    private class FakeReminderNotifier : ReminderNotifier {
        var lastSchedule: CollectionSchedule? = null

        override suspend fun notify(schedule: CollectionSchedule) {
            lastSchedule = schedule
        }
    }
}
