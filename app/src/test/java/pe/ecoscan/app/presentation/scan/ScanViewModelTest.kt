package pe.ecoscan.app.presentation.scan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class ScanViewModelTest {

    @Test
    fun `el estado inicial no esta cargando y no tiene error`() {
        val viewModel = ScanViewModel()

        val uiState = viewModel.uiState.value
        assertFalse(uiState.isLoading)
        assertNull(uiState.errorMessage)
        assertEquals(ScanUiState(), uiState)
    }
}
