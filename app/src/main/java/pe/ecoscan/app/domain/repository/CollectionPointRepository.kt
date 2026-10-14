package pe.ecoscan.app.domain.repository

import pe.ecoscan.app.core.common.Resource
import pe.ecoscan.app.domain.model.CollectionPoint
import pe.ecoscan.app.domain.model.GeoPoint

interface CollectionPointRepository {

    // Puntos de acopio dentro del radio indicado, del más cercano al más lejano. Si no hay
    // internet devuelve los últimos puntos guardados en el dispositivo (Resource.Success con
    // isStale = true); si tampoco hay, devuelve un Resource.Error.
    suspend fun getNearbyPoints(center: GeoPoint, radiusMeters: Int): Resource<List<CollectionPoint>>
}