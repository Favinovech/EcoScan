package pe.ecoscan.app.domain.model

data class User(
    val uid: String,
    val email: String,
    val isEmailVerified: Boolean = true
)