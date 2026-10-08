package pe.ecoscan.app.domain.model

// Distingue qué produjo el escaneo: la clasificación de residuos por imagen (HU04) o
// la lectura de un código de barras de empaque (HU05).
sealed class ScanResult {
    data class WasteClassified(val classification: WasteClassification) : ScanResult()
    data class BarcodeDetected(val barcode: String) : ScanResult()
}
