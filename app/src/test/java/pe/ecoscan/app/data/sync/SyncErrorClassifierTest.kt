package pe.ecoscan.app.data.sync

import com.google.firebase.firestore.FirebaseFirestoreException
import org.junit.Assert.assertEquals
import org.junit.Test
import pe.ecoscan.app.domain.sync.SyncErrorType
import java.io.IOException

class SyncErrorClassifierTest {

    @Test
    fun `un error de red es transitorio`() {
        assertEquals(SyncErrorType.TRANSIENT, classifySyncError(IOException("Sin conexión")))
    }

    @Test
    fun `un error de firestore por servidor no disponible es transitorio`() {
        val error = FirebaseFirestoreException("No disponible", FirebaseFirestoreException.Code.UNAVAILABLE)

        assertEquals(SyncErrorType.TRANSIENT, classifySyncError(error))
    }

    @Test
    fun `un error de firestore por datos invalidos no es transitorio`() {
        val error = FirebaseFirestoreException("Datos inválidos", FirebaseFirestoreException.Code.INVALID_ARGUMENT)

        assertEquals(SyncErrorType.PERMANENT, classifySyncError(error))
    }

    @Test
    fun `un error generico no es transitorio`() {
        assertEquals(SyncErrorType.PERMANENT, classifySyncError(IllegalStateException("Dato inválido")))
    }
}
