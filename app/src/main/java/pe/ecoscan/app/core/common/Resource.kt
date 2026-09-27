package pe.ecoscan.app.core.common

// Envoltorio genérico para representar el estado de una operación asíncrona.
sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
}
