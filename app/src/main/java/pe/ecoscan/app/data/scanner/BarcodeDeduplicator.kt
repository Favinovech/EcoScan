package pe.ecoscan.app.data.scanner

// Evita que el mismo código de barras se vuelva a emitir si fue detectado hace menos
// de VENTANA_MS: pasado ese tiempo, el mismo código vuelve a considerarse "nuevo".
class BarcodeDeduplicator(
    private val clock: () -> Long = System::currentTimeMillis
) {

    private val lastSeenAtMillis = mutableMapOf<String, Long>()

    fun shouldEmit(code: String): Boolean {
        val now = clock()
        val lastSeen = lastSeenAtMillis[code]
        if (lastSeen != null && now - lastSeen < VENTANA_MS) {
            return false
        }
        lastSeenAtMillis[code] = now
        return true
    }

    companion object {
        const val VENTANA_MS = 3_000L
    }
}
