package pe.ecoscan.app.domain.usecase

import pe.ecoscan.app.core.common.Resource
import pe.ecoscan.app.domain.model.CollectionPoint
import pe.ecoscan.app.domain.model.GeoPoint
import pe.ecoscan.app.domain.repository.CollectionPointRepository
import javax.inject.Inject

class GetNearbyCollectionPointsUseCase @Inject constructor(
    private val repository: CollectionPointRepository
) {
    suspend operator fun invoke(center: GeoPoint, radiusMeters: Int): Resource<List<CollectionPoint>> =
        repository.getNearbyPoints(center, radiusMeters)
}