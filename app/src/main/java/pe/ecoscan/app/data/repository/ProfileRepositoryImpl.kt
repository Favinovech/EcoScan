package pe.ecoscan.app.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.data.local.dao.UserProfileDao
import pe.ecoscan.app.data.mapper.toDomain
import pe.ecoscan.app.data.mapper.toEntity
import pe.ecoscan.app.data.mapper.toFirestoreMap
import pe.ecoscan.app.domain.model.User
import pe.ecoscan.app.domain.model.UserProfile
import pe.ecoscan.app.domain.repository.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

private const val USERS_COLLECTION = "users"
private const val PHOTOS_FOLDER = "profile_photos"

// Las tareas de escritura de Firestore no terminan hasta que el servidor confirma, así que
// sin conexión se quedarían esperando para siempre. Con este tiempo límite pasamos a
// "guardado solo en el dispositivo" en lugar de dejar la pantalla cargando.
private const val REMOTE_TIMEOUT_MS = 10_000L

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: UserProfileDao,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val dispatcherProvider: DispatcherProvider
) : ProfileRepository {

    override fun observeProfile(uid: String): Flow<UserProfile?> =
        profileDao.observe(uid).map { it?.toDomain() }

    override suspend fun refreshProfile(user: User): Result<Unit> = withContext(dispatcherProvider.io) {
        try {
            // Si hay cambios locales sin subir, no los pisamos con lo que haya en la nube.
            val local = profileDao.get(user.uid)
            if (local != null && !local.isSynced) return@withContext Result.success(Unit)

            val snapshot = withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                firestore.collection(USERS_COLLECTION).document(user.uid).get().await()
            } ?: return@withContext Result.failure(Exception("Sin conexión"))

            if (snapshot.exists()) {
                profileDao.upsert(
                    UserProfile(
                        uid = user.uid,
                        email = user.email,
                        displayName = snapshot.getString("displayName").orEmpty(),
                        district = snapshot.getString("district").orEmpty(),
                        photoUrl = snapshot.getString("photoUrl"),
                        notificationsEnabled = snapshot.getBoolean("notificationsEnabled") ?: true,
                        weeklyGoalKg = snapshot.getLong("weeklyGoalKg")?.toInt()
                            ?: UserProfile.DEFAULT_WEEKLY_GOAL_KG,
                        isSynced = true
                    ).toEntity()
                )
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveProfile(
        profile: UserProfile,
        newPhotoUri: String?
    ): Result<UserProfile> = withContext(dispatcherProvider.io) {
        // 1) Siempre se guarda primero en Room: así el usuario no pierde sus cambios.
        try {
            profileDao.upsert(profile.copy(isSynced = false).toEntity())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }

        // 2) Luego se intenta subir a Firebase. Si falla o no hay red, queda pendiente.
        try {
            val syncedProfile = withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                pushToRemote(profile, newPhotoUri)
            }
            Result.success(syncedProfile ?: profile.copy(isSynced = false))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.success(profile.copy(isSynced = false))
        }
    }

    private suspend fun pushToRemote(profile: UserProfile, newPhotoUri: String?): UserProfile {
        var photoUrl = profile.photoUrl
        if (newPhotoUri != null) {
            val photoRef = storage.reference.child("$PHOTOS_FOLDER/${profile.uid}.jpg")
            photoRef.putFile(Uri.parse(newPhotoUri)).await()
            photoUrl = photoRef.downloadUrl.await().toString()
        }
        val synced = profile.copy(photoUrl = photoUrl, isSynced = true)
        firestore.collection(USERS_COLLECTION).document(profile.uid)
            .set(synced.toFirestoreMap())
            .await()
        profileDao.markSynced(profile.uid, photoUrl)
        return synced
    }

    override suspend fun deleteProfile(uid: String): Result<Unit> = withContext(dispatcherProvider.io) {
        try {
            val deleted = withTimeoutOrNull(REMOTE_TIMEOUT_MS) {
                firestore.collection(USERS_COLLECTION).document(uid).delete().await()
                try {
                    storage.reference.child("$PHOTOS_FOLDER/$uid.jpg").delete().await()
                } catch (e: StorageException) {
                    // Si el usuario nunca subió foto, no hay nada que borrar.
                    if (e.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) throw e
                }
                true
            }
            if (deleted == null) {
                return@withContext Result.failure(Exception("Sin conexión. Inténtalo de nuevo"))
            }
            profileDao.deleteByUid(uid)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}