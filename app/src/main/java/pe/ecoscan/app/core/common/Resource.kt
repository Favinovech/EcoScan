package pe.ecoscan.app.core.common

// Envoltorio genérico para representar el estado de una operación asíncrona.
// isStale e ErrorType.type tienen valor por defecto para no romper el código existente
// que ya construye Resource.Success/Resource.Error sin esos parámetros.
sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()
    data class Success<T>(val data: T, val isStale: Boolean = false) : Resource<T>()
    data class Error(val message: String, val type: ErrorType = ErrorType.UNKNOWN) : Resource<Nothing>()
}
