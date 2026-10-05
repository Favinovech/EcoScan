package pe.ecoscan.app.domain.usecase

import android.graphics.Bitmap
import pe.ecoscan.app.domain.model.WasteClassification
import pe.ecoscan.app.domain.repository.WasteClassifier
import javax.inject.Inject

class ClassifyWasteUseCase @Inject constructor(
    private val classifier: WasteClassifier
) {
    suspend operator fun invoke(bitmap: Bitmap): Result<WasteClassification> {
        return classifier.classify(bitmap)
    }
}