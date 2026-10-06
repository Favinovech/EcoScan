package pe.ecoscan.app.domain.usecase

import pe.ecoscan.app.domain.repository.AuthRepository
import pe.ecoscan.app.domain.repository.ProfileRepository
import javax.inject.Inject

// Eliminar la cuenta es una operación sensible: Firebase exige una sesión reciente, así que
// primero se reautentica con la contraseña. Luego se borran los datos del perfil (mientras
// la sesión sigue activa, para pasar las reglas de seguridad) y al final la cuenta de Auth.
class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(uid: String, password: String): Result<Unit> {
        authRepository.reauthenticate(password).onFailure { return Result.failure(it) }
        profileRepository.deleteProfile(uid).onFailure { return Result.failure(it) }
        return authRepository.deleteAccount()
    }
}