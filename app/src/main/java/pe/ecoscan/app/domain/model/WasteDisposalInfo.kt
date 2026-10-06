package pe.ecoscan.app.domain.model

object WasteDisposalInfo {
    fun getInstructions(category: WasteCategory): String = when (category) {
        WasteCategory.PLASTIC -> "Enjuaga, seca y aplasta el envase para reducir volumen. Deposita en el contenedor blanco."
        WasteCategory.GLASS -> "Enjuaga y retira tapas o corchos. No mezcles con cerámica o bombillas. Deposita en el contenedor gris."
        WasteCategory.PAPER -> "Asegúrate de que esté limpio y seco, sin grasa ni humedad. Deposita en el contenedor azul."
        WasteCategory.METAL -> "Enjuaga latas y retira restos orgánicos. Aplasta si es posible. Deposita en el contenedor amarillo."
        WasteCategory.ORGANIC -> "Restos de comida, cáscaras y hojas. Apto para compostaje. Deposita en el contenedor marrón."
        WasteCategory.HAZARDOUS -> "Pilas, baterías, focos, aerosoles o envases químicos. Requiere disposición especial en contenedor rojo."
    }

    fun getContainerColorName(category: WasteCategory): String = when (category) {
        WasteCategory.PLASTIC -> "Contenedor Blanco"
        WasteCategory.GLASS -> "Contenedor Gris"
        WasteCategory.PAPER -> "Contenedor Azul"
        WasteCategory.METAL -> "Contenedor Amarillo"
        WasteCategory.ORGANIC -> "Contenedor Marrón"
        WasteCategory.HAZARDOUS -> "Contenedor Rojo"
    }

    fun getDefaultPoints(category: WasteCategory): Int = when (category) {
        WasteCategory.PLASTIC -> 15
        WasteCategory.GLASS -> 20
        WasteCategory.PAPER -> 10
        WasteCategory.METAL -> 25
        WasteCategory.ORGANIC -> 5
        WasteCategory.HAZARDOUS -> 30
    }
}