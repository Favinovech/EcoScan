package pe.ecoscan.app.domain.usecase

import pe.ecoscan.app.domain.model.GeoPoint
import pe.ecoscan.app.domain.repository.LocationRepository
import javax.inject.Inject

class GetCurrentLocationUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(): Result<GeoPoint> = repository.getCurrentLocation()
}