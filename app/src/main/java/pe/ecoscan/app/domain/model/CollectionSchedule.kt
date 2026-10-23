package pe.ecoscan.app.domain.model

import java.time.DayOfWeek

// Día y hora en que pasa la recolección de reciclables en un distrito (HU09).
data class CollectionSchedule(
    val district: String,
    val dayOfWeek: DayOfWeek,
    val hour: Int,
    val minute: Int = 0
)
