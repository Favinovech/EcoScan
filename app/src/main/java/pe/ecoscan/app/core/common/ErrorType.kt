package pe.ecoscan.app.core.common

// Tipo de fallo detrás de un Resource.Error, para que la UI pueda reaccionar distinto
// (ej. reintentar en timeout, pedir conexión, mostrar "no encontrado").
enum class ErrorType {
    NOT_FOUND,
    NO_CONNECTION,
    TIMEOUT,
    SERVER_ERROR,
    UNKNOWN
}
