package pe.ecoscan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pe.ecoscan.app.domain.model.User
import pe.ecoscan.app.domain.model.UserProfile
import pe.ecoscan.app.domain.repository.ProfileRepository

// Repositorio en memoria solo para pruebas unitarias (sin Room ni Firebase).
class FakeProfileRepository : ProfileRepository {

    val profileFlow = MutableStateFlow<UserProfile?>(null)
    var lastSavedProfile: UserProfile? = null
    var lastSavedPhotoUri: String? = null

    override fun observeProfile(uid: String): Flow<UserProfile?> = profileFlow

    override suspend fun refreshProfile(user: User): Result<Unit> = Result.success(Unit)

    override suspend fun saveProfile(
        profile: UserProfile,
        newPhotoUri: String?
    ): Result<UserProfile> {
        lastSavedProfile = profile
        lastSavedPhotoUri = newPhotoUri
        profileFlow.value = profile
        return Result.success(profile)
    }

    override suspend fun deleteProfile(uid: String): Result<Unit> {
        profileFlow.value = null
        return Result.success(Unit)
    }
}