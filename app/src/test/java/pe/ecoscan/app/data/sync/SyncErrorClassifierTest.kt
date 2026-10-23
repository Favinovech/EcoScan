package pe.ecoscan.app.data.sync

import com.google.firebase.firestore.FirebaseFirestoreException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class SyncErrorClassifierTest {

    @Test
    fun `un error de red es transitorio`() {
        assertTrue(isTransientSyncError(IOException("Sin conexión")))
    }

    @Test
    fun `un error de firestore por servidor no disponible es transitorio`() {
        val error = FirebaseFirestoreException("No disponible", FirebaseFirestoreException.Code.UNAVAILABLE)

        assertTrue(isTransientSyncError(error))
    }

    @Test
    fun `un error de firestore por datos invalidos no es transitorio`() {
        val error = FirebaseFirestoreException("Datos inválidos", FirebaseFirestoreException.Code.INVALID_ARGUMENT)

        assertFalse(isTransientSyncError(error))
    }

    @Test
    fun `un error generico no es transitorio`() {
        assertFalse(isTransientSyncError(IllegalStateException("Dato inválido")))
    }
}
