package pe.ecoscan.app.data.repository

import pe.ecoscan.app.domain.model.CollectionSchedule
import pe.ecoscan.app.domain.repository.CollectionScheduleRepository
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton

// HORARIOS DE EJEMPLO. Son ilustrativos para esta parte de la HU09: los horarios reales de
// recolección por distrito los define la asociación de recicladores que opera los puntos de
// acopio, no esta app. Cuando esa información exista (API, panel administrativo, etc.), este
// es el único archivo que habría que cambiar para reemplazar los datos de ejemplo.
private val SAMPLE_SCHEDULES = listOf(
    CollectionSchedule(district = "Miraflores", dayOfWeek = DayOfWeek.MONDAY, hour = 8),
    CollectionSchedule(district = "San Isidro", dayOfWeek = DayOfWeek.TUESDAY, hour = 9),
    CollectionSchedule(district = "Surco", dayOfWeek = DayOfWeek.WEDNESDAY, hour = 8),
    CollectionSchedule(district = "Barranco", dayOfWeek = DayOfWeek.THURSDAY, hour = 10),
    CollectionSchedule(district = "La Molina", dayOfWeek = DayOfWeek.FRIDAY, hour = 8)
).associateBy { it.district }

// Horario que se usa cuando el distrito del perfil no está en la lista de ejemplo.
private val DEFAULT_SCHEDULE = CollectionSchedule(district = "Lima", dayOfWeek = DayOfWeek.MONDAY, hour = 8)

@Singleton
class CollectionScheduleRepositoryImpl @Inject constructor() : CollectionScheduleRepository {

    override fun getSchedule(district: String): CollectionSchedule =
        SAMPLE_SCHEDULES[district]
            ?: DEFAULT_SCHEDULE.copy(district = district.ifBlank { DEFAULT_SCHEDULE.district })
}
