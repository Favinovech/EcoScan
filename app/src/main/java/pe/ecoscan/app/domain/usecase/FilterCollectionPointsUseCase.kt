package pe.ecoscan.app.domain.usecase

import pe.ecoscan.app.domain.model.CollectionPoint
import pe.ecoscan.app.domain.model.WasteCategory
import javax.inject.Inject

// Filtros del mapa (HU07): por material aceptado y por distancia máxima.
// Con un material elegido solo quedan los puntos que declaran recibirlo; los que no
// especifican materiales se muestran solo cuando no hay filtro de material.
class FilterCollectionPointsUseCase @Inject constructor() {
    operator fun invoke(
        points: List<CollectionPoint>,
        material: WasteCategory?,
        maxDistanceMeters: Int?
    ): List<CollectionPoint> = points.filter { point ->
        (material == null || material in point.acceptedMaterials) &&
                (maxDistanceMeters == null || point.distanceMeters <= maxDistanceMeters)
    }
}