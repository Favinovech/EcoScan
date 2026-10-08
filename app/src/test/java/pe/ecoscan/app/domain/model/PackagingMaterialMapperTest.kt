package pe.ecoscan.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PackagingMaterialMapperTest {

    @Test
    fun `reconoce plastico en texto libre y en tags`() {
        val resolution = PackagingMaterialMapper.resolve("Botella de Plástico PET", listOf("en:plastic"))

        assertEquals(listOf("Plástico"), resolution.packagingMaterials)
        assertTrue(resolution.disposalHint.isNotBlank())
    }

    @Test
    fun `reconoce vidrio`() {
        val resolution = PackagingMaterialMapper.resolve(null, listOf("en:glass"))

        assertEquals(listOf("Vidrio"), resolution.packagingMaterials)
    }

    @Test
    fun `reconoce papel y carton`() {
        val resolution = PackagingMaterialMapper.resolve("Caja de cartón", emptyList())

        assertEquals(listOf("Papel y cartón"), resolution.packagingMaterials)
    }

    @Test
    fun `reconoce metal y aluminio`() {
        val resolution = PackagingMaterialMapper.resolve(null, listOf("en:aluminium", "en:metal"))

        assertEquals(listOf("Metal y aluminio"), resolution.packagingMaterials)
    }

    @Test
    fun `reconoce tetrapak como material distinto`() {
        val resolution = PackagingMaterialMapper.resolve("Tetra Pak", emptyList())

        assertEquals(listOf("Tetrapak"), resolution.packagingMaterials)
    }

    @Test
    fun `combina varios materiales reconocidos sin duplicar`() {
        val resolution = PackagingMaterialMapper.resolve("Plástico", listOf("en:plastic", "en:glass"))

        assertEquals(listOf("Plástico", "Vidrio"), resolution.packagingMaterials)
    }

    @Test
    fun `etiqueta desconocida devuelve indicacion neutra sin error`() {
        val resolution = PackagingMaterialMapper.resolve("xyz-material-desconocido", listOf("fr:inconnu"))

        assertEquals(emptyList<String>(), resolution.packagingMaterials)
        assertTrue(resolution.disposalHint.isNotBlank())
    }

    @Test
    fun `sin informacion de empaque devuelve indicacion neutra`() {
        val resolution = PackagingMaterialMapper.resolve(null, emptyList())

        assertEquals(emptyList<String>(), resolution.packagingMaterials)
        assertTrue(resolution.disposalHint.isNotBlank())
    }
}
