package pe.ecoscan.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pe.ecoscan.app.data.local.entity.CachedCollectionPointEntity

@Dao
interface CachedCollectionPointDao {

    // Puntos dentro de un rectángulo de coordenadas. El repositorio lo usa como primer
    // recorte y luego descarta los que quedan fuera del círculo con la distancia real.
    @Query(
        "SELECT * FROM cached_collection_points " +
                "WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLon AND :maxLon"
    )
    suspend fun getInBounds(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): List<CachedCollectionPointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(points: List<CachedCollectionPointEntity>)

    @Query("DELETE FROM cached_collection_points WHERE cachedAt < :thresholdMillis")
    suspend fun deleteOlderThan(thresholdMillis: Long)
}