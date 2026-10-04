package pe.ecoscan.app.domain.usecase

import pe.ecoscan.app.domain.model.User
import pe.ecoscan.app.domain.repository.ProfileRepository
import javax.inject.Inject

class RefreshUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(user: User): Result<Unit> = repository.refreshProfile(user)
}