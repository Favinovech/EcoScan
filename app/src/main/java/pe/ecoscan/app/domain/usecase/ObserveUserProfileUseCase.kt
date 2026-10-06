package pe.ecoscan.app.domain.usecase

import kotlinx.coroutines.flow.Flow
import pe.ecoscan.app.domain.model.UserProfile
import pe.ecoscan.app.domain.repository.ProfileRepository
import javax.inject.Inject

class ObserveUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(uid: String): Flow<UserProfile?> = repository.observeProfile(uid)
}