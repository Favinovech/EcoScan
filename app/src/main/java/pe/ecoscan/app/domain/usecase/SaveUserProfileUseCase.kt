package pe.ecoscan.app.domain.usecase

import pe.ecoscan.app.domain.model.UserProfile
import pe.ecoscan.app.domain.repository.ProfileRepository
import javax.inject.Inject

// Valida los datos del formulario y delega el guardado en el repositorio.
class SaveUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(profile: UserProfile, newPhotoUri: String?): Result<UserProfile> {
        val name = profile.displayName.trim()
        val district = profile.district.trim()
        return when {
            name.isEmpty() ->
                Result.failure(IllegalArgumentException("Ingresa tu nombre"))
            name.length > UserProfile.MAX_NAME_LENGTH ->
                Result.failure(IllegalArgumentException("El nombre es demasiado largo"))
            district.isEmpty() ->
                Result.failure(IllegalArgumentException("Ingresa tu distrito"))
            district.length > UserProfile.MAX_DISTRICT_LENGTH ->
                Result.failure(IllegalArgumentException("El distrito es demasiado largo"))
            profile.weeklyGoalKg !in UserProfile.MIN_WEEKLY_GOAL_KG..UserProfile.MAX_WEEKLY_GOAL_KG ->
                Result.failure(IllegalArgumentException("La meta semanal debe estar entre 1 y 50 kg"))
            else -> repository.saveProfile(
                profile.copy(displayName = name, district = district),
                newPhotoUri
            )
        }
    }
}