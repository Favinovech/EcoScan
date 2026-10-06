package pe.ecoscan.app.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.ecoscan.app.domain.model.User
import pe.ecoscan.app.domain.model.UserProfile

interface ProfileRepository {

    // Perfil cacheado localmente (Room). Emite null si aún no existe.
    fun observeProfile(uid: String): Flow<UserProfile?>

    // Descarga el perfil desde Firestore y lo guarda en Room. Falla en silencio si no hay red.
    suspend fun refreshProfile(user: User): Result<Unit>

    // Guarda primero en Room y luego intenta subir a Firebase (foto en Storage, datos en
    // Firestore). Devuelve el perfil resultante: isSynced = false si no hubo conexión.
    // newPhotoUri es un Uri en texto (content:// o file://) de la foto recién elegida.
    suspend fun saveProfile(profile: UserProfile, newPhotoUri: String?): Result<UserProfile>

    // Borra el perfil en Firestore, la foto en Storage y la copia local.
    suspend fun deleteProfile(uid: String): Result<Unit>
}