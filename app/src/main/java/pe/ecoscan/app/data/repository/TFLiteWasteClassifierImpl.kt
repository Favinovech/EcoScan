package pe.ecoscan.app.data.repository

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import pe.ecoscan.app.core.common.DispatcherProvider
import pe.ecoscan.app.domain.model.WasteCategory
import pe.ecoscan.app.domain.model.WasteClassification
import pe.ecoscan.app.domain.model.WasteDisposalInfo
import pe.ecoscan.app.domain.repository.WasteClassifier
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "TFLiteClassifier"
private const val MODEL_FILE = "waste_classifier.tflite"
private const val LABELS_FILE = "labels.txt"
private const val INPUT_SIZE = 224
private const val PIXEL_SIZE = 3
private const val IMAGE_MEAN = 127.5f
private const val IMAGE_STD = 127.5f

@Singleton
class TFLiteWasteClassifierImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatcherProvider: DispatcherProvider
) : WasteClassifier {

    private val interpreter: Interpreter by lazy {
        val modelBuffer = loadModelFromAssets(MODEL_FILE)
        val options = Interpreter.Options().apply {
            setNumThreads(2)
        }
        Interpreter(modelBuffer, options)
    }

    private val labels: List<String> by lazy {
        loadLabelsFile(LABELS_FILE)
    }

    override suspend fun classify(bitmap: Bitmap): Result<WasteClassification> = withContext(dispatcherProvider.default) {
        try {
            // 1. Redimensionar imagen a 224x224
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)

            // 2. Convertir imagen a ByteBuffer normalizado (Float32)
            val inputBuffer = convertBitmapToByteBuffer(scaledBitmap)

            // 3. Determinar número de clases real desde el tensor de salida del modelo
            val outputTensor = interpreter.getOutputTensor(0)
            val numClasses = outputTensor.shape()[1] // Ej. 5 clases

            val outputProbabilities = Array(1) { FloatArray(numClasses) }

            // 4. Inferencia real ejecutada por TensorFlow Lite
            interpreter.run(inputBuffer, outputProbabilities)

            // 5. Encontrar la clase con mayor probabilidad
            val probabilities = outputProbabilities[0]
            var maxIndex = 0
            var maxConfidence = probabilities[0]
            for (i in 1 until probabilities.size) {
                if (probabilities[i] > maxConfidence) {
                    maxConfidence = probabilities[i]
                    maxIndex = i
                }
            }

            Log.d(TAG, "Inferencia completada. Índice ganador: $maxIndex con confianza: $maxConfidence")

            // 6. Obtener etiqueta
            val rawLabel = labels.getOrElse(maxIndex) { "plastic" }.lowercase()
            val cleanLabel = rawLabel.replace(Regex("^[0-9]+\\s*"), "").trim()
            val category = mapLabelToCategory(cleanLabel)

            Result.success(
                WasteClassification(
                    category = category,
                    confidence = maxConfidence.coerceIn(0f, 1f),
                    instructions = WasteDisposalInfo.getInstructions(category),
                    recommendedContainerColor = WasteDisposalInfo.getContainerColorName(category)
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error en la inferencia real de TFLite: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun loadModelFromAssets(fileName: String): ByteBuffer {
        val fileDescriptor: AssetFileDescriptor = context.assets.openFd(fileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun loadLabelsFile(fileName: String): List<String> {
        return context.assets.open(fileName).bufferedReader().useLines { lines ->
            lines.map { it.trim() }.filter { it.isNotEmpty() }.toList()
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        // 4 bytes por float * 224 * 224 * 3 canales
        val byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var pixel = 0
        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {
                val value = intValues[pixel++]
                // Normalización estándar [-1, 1] de MobileNet / Teachable Machine
                byteBuffer.putFloat((((value shr 16) and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                byteBuffer.putFloat((((value shr 8) and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                byteBuffer.putFloat(((value and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
            }
        }
        byteBuffer.rewind()
        return byteBuffer
    }

    private fun mapLabelToCategory(label: String): WasteCategory = when {
        label.contains("plastic") || label.contains("plastico") -> WasteCategory.PLASTIC
        label.contains("glass") || label.contains("vidrio") -> WasteCategory.GLASS
        label.contains("paper") || label.contains("papel") || label.contains("carton") -> WasteCategory.PAPER
        label.contains("metal") || label.contains("lata") -> WasteCategory.METAL
        label.contains("organic") || label.contains("organico") -> WasteCategory.ORGANIC
        else -> WasteCategory.PLASTIC
    }
}