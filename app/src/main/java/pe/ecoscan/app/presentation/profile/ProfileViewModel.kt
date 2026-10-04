package pe.ecoscan.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.domain.model.User
import pe.ecoscan.app.domain.model.UserProfile
import pe.ecoscan.app.domain.repository.AuthRepository
import pe.ecoscan.app.domain.usecase.DeleteAccountUseCase
import pe.ecoscan.app.domain.usecase.ObserveUserProfileUseCase
import pe.ecoscan.app.domain.usecase.RefreshUserProfileUseCase
import pe.ecoscan.app.domain.usecase.SaveUserProfileUseCase
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val observeUserProfileUseCase: ObserveUserProfileUseCase,
    private val refreshUserProfileUseCase: RefreshUserProfileUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var currentUser: User? = null

    // Último perfil conocido (Room). Sirve para saber si el formulario tiene cambios.
    private var baseline: UserProfile? = null

    init {
        observeProfile()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeProfile() {
        authRepository.currentUser
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { user ->
                currentUser = user
                _uiState.update { it.copy(email = user.email, isLoading = true) }
                refreshInBackground(user)
            }
            .flatMapLatest { user -> observeUserProfileUseCase(user.uid) }
            .onEach { profile -> onProfileEmitted(profile) }
            .catch { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Error al cargar el perfil")
                }
            }
            .launchIn(viewModelScope)
    }

    // Intenta traer la versión de Firestore. Si no hay red no pasa nada: se muestra lo cacheado.
    private fun refreshInBackground(user: User) {
        viewModelScope.launch(dispatcherProvider.main) {
            refreshUserProfileUseCase(user)
        }
    }

    private fun onProfileEmitted(profile: UserProfile?) {
        baseline = profile
        _uiState.update { state ->
            when {
                profile == null -> state.copy(isLoading = false)
                // Si el usuario está editando, no le pisamos lo que escribió.
                state.hasUnsavedChanges -> state.copy(isLoading = false, isSynced = profile.isSynced)
                else -> state.applyProfile(profile).copy(isLoading = false)
            }
        }
    }

    fun onDisplayNameChange(value: String) {
        _uiState.update {
            it.copy(displayName = value.take(UserProfile.MAX_NAME_LENGTH)).withDirtyFlag()
        }
    }

    fun onDistrictChange(value: String) {
        _uiState.update {
            it.copy(district = value.take(UserProfile.MAX_DISTRICT_LENGTH)).withDirtyFlag()
        }
    }

    fun onNotificationsChange(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled).withDirtyFlag() }
    }

    fun onWeeklyGoalChange(goalKg: Int) {
        val clamped = goalKg.coerceIn(
            UserProfile.MIN_WEEKLY_GOAL_KG,
            UserProfile.MAX_WEEKLY_GOAL_KG
        )
        _uiState.update { it.copy(weeklyGoalKg = clamped).withDirtyFlag() }
    }

    fun onPhotoSelected(uri: String) {
        _uiState.update { it.copy(pendingPhotoUri = uri).withDirtyFlag() }
    }

    fun saveProfile() {
        val user = currentUser ?: return
        val state = _uiState.value
        if (state.isSaving) return

        viewModelScope.launch(dispatcherProvider.main) {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, infoMessage = null) }
            val draft = UserProfile(
                uid = user.uid,
                email = user.email,
                displayName = state.displayName,
                district = state.district,
                photoUrl = state.photoUrl,
                notificationsEnabled = state.notificationsEnabled,
                weeklyGoalKg = state.weeklyGoalKg
            )
            saveUserProfileUseCase(draft, state.pendingPhotoUri)
                .onSuccess { saved ->
                    baseline = saved
                    _uiState.update { current ->
                        // Si la foto no se pudo subir (sin conexión) se conserva para reintentar.
                        val keepPhoto = current.pendingPhotoUri != null && !saved.isSynced
                        current.applyProfile(saved).copy(
                            isSaving = false,
                            pendingPhotoUri = if (keepPhoto) current.pendingPhotoUri else null,
                            infoMessage = if (saved.isSynced) {
                                "Perfil actualizado"
                            } else {
                                "Sin conexión: guardamos tus cambios en el dispositivo. " +
                                        "Vuelve a guardar cuando tengas internet."
                            }
                        ).withDirtyFlag()
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message ?: "No se pudo guardar el perfil"
                        )
                    }
                }
        }
    }

    fun signOut() {
        viewModelScope.launch(dispatcherProvider.main) {
            authRepository.signOut()
            _uiState.update { it.copy(isSignedOut = true) }
        }
    }

    fun deleteAccount(password: String) {
        val user = currentUser ?: return
        if (_uiState.value.isDeleting) return

        viewModelScope.launch(dispatcherProvider.main) {
            _uiState.update { it.copy(isDeleting = true, errorMessage = null, infoMessage = null) }
            deleteAccountUseCase(user.uid, password)
                .onSuccess {
                    _uiState.update { state -> state.copy(isDeleting = false, isSignedOut = true) }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isDeleting = false,
                            errorMessage = error.message ?: "No se pudo eliminar la cuenta"
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    private fun ProfileUiState.applyProfile(profile: UserProfile): ProfileUiState = copy(
        displayName = profile.displayName,
        district = profile.district,
        photoUrl = profile.photoUrl,
        notificationsEnabled = profile.notificationsEnabled,
        weeklyGoalKg = profile.weeklyGoalKg,
        isSynced = profile.isSynced
    )

    // Compara el formulario con el último perfil guardado para habilitar/deshabilitar "Guardar".
    private fun ProfileUiState.withDirtyFlag(): ProfileUiState {
        val base = baseline
        val dirty = pendingPhotoUri != null ||
                displayName != (base?.displayName ?: "") ||
                district != (base?.district ?: "") ||
                notificationsEnabled != (base?.notificationsEnabled ?: true) ||
                weeklyGoalKg != (base?.weeklyGoalKg ?: UserProfile.DEFAULT_WEEKLY_GOAL_KG)
        return copy(hasUnsavedChanges = dirty)
    }
}
