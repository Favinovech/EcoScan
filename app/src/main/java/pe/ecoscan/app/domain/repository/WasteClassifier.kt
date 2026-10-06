package pe.ecoscan.app.domain.repository

import android.graphics.Bitmap
import pe.ecoscan.app.domain.model.WasteClassification

interface WasteClassifier {
    // Clasifica la imagen del residuo en menos de 3 segundos de forma offline.
    suspend fun classify(bitmap: Bitmap): Result<WasteClassification>
}