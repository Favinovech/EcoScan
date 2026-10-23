package pe.ecoscan.app.domain.repository

import pe.ecoscan.app.domain.model.CollectionSchedule

interface CollectionScheduleRepository {

    // Horario de recolección para el distrito dado. Si el distrito no está en la lista
    // conocida, devuelve un horario por defecto en vez de null, para que siempre se pueda
    // programar un recordatorio.
    fun getSchedule(district: String): CollectionSchedule
}
