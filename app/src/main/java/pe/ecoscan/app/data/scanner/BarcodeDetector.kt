package pe.ecoscan.app.data.scanner

import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import pe.ecoscan.app.core.common.DispatcherProvider
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Lee códigos de barras de producto sobre el mismo bitmap que ya captura el botón de
// escaneo de la HU04, sin un ImageAnalysis.Analyzer propio ni un executor dedicado.
@Singleton
class BarcodeDetector @Inject constructor(
    private val dispatcherProvider: DispatcherProvider
) {

    private val deduplicator = BarcodeDeduplicator()

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E
        )
        .build()

    // Perezoso: BarcodeScanning.getClient() exige que MlKitContext ya esté inicializado,
    // lo que no ocurre en un test JVM plano. Al diferirlo, construir un BarcodeDetector
    // (por ejemplo, como valor por defecto de ScanViewModel) no falla si nunca se llama a detect().
    private val scanner by lazy { BarcodeScanning.getClient(options) }

    suspend fun detect(bitmap: Bitmap): Result<String?> = withContext(dispatcherProvider.default) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val barcodes = scanner.process(image).await()
            val code = barcodes.firstOrNull { it.rawValue != null }?.rawValue
            Result.success(code?.takeIf { deduplicator.shouldEmit(it) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { continuation.resume(it) }
        addOnFailureListener { continuation.resumeWithException(it) }
    }

    companion object {
        val SUPPORTED_FORMATS = listOf(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E
        )
    }
}
