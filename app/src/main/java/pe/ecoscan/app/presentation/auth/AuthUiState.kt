package pe.ecoscan.app.presentation.auth

import pe.ecoscan.app.domain.model.User

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)