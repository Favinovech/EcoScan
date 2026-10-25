package pe.ecoscan.app.domain.sync

// Tope de intentos para un error transitorio. Sin este límite, un fallo de red que nunca se
// resuelve (por ejemplo, el usuario nunca vuelve a tener conexión) haría que WorkManager
// reintentara para siempre, gastando batería con el backoff exponencial de SyncScheduler.
const val MAX_SYNC_ATTEMPTS = 5

sealed class RetryDecision {
    data object Retry : RetryDecision()
    data object Fail : RetryDecision()
}

// Función pura: decide si vale la pena reintentar según el tipo de error y cuántas veces ya se
// intentó. "attempt" es el número del intento que ACABA de fallar (1 = el primero), no el
// contador de WorkManager (que arranca en 0); quien llama es responsable de sumarle 1.
fun decideRetry(
    errorType: SyncErrorType,
    attempt: Int,
    maxAttempts: Int = MAX_SYNC_ATTEMPTS
): RetryDecision = when {
    errorType == SyncErrorType.PERMANENT -> RetryDecision.Fail
    attempt < maxAttempts -> RetryDecision.Retry
    else -> RetryDecision.Fail
}
