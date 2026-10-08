package pe.ecoscan.app.data.scanner

import com.google.mlkit.vision.barcode.common.Barcode
import org.junit.Assert.assertEquals
import org.junit.Test

class BarcodeDetectorFormatsTest {

    @Test
    fun `los formatos soportados son los de producto`() {
        val expected = listOf(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E
        )

        assertEquals(expected, BarcodeDetector.SUPPORTED_FORMATS)
    }
}
