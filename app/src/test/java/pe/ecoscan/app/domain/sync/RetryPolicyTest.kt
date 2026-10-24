package pe.ecoscan.app.domain.sync

import org.junit.Assert.assertEquals
import org.junit.Test

class RetryPolicyTest {

    @Test
    fun `un error transitorio por debajo del tope reintenta`() {
        val decision = decideRetry(SyncErrorType.TRANSIENT, attempt = 1, maxAttempts = 5)

        assertEquals(RetryDecision.Retry, decision)
    }

    @Test
    fun `un error transitorio que llega al tope falla`() {
        val decision = decideRetry(SyncErrorType.TRANSIENT, attempt = 5, maxAttempts = 5)

        assertEquals(RetryDecision.Fail, decision)
    }

    @Test
    fun `un error transitorio que ya paso el tope falla`() {
        val decision = decideRetry(SyncErrorType.TRANSIENT, attempt = 6, maxAttempts = 5)

        assertEquals(RetryDecision.Fail, decision)
    }

    @Test
    fun `un error permanente falla incluso en el primer intento`() {
        val decision = decideRetry(SyncErrorType.PERMANENT, attempt = 1, maxAttempts = 5)

        assertEquals(RetryDecision.Fail, decision)
    }

    @Test
    fun `sin indicar el tope se usa MAX_SYNC_ATTEMPTS`() {
        val decision = decideRetry(SyncErrorType.TRANSIENT, attempt = MAX_SYNC_ATTEMPTS)

        assertEquals(RetryDecision.Fail, decision)
    }
}
