package pe.ecoscan.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pe.ecoscan.app.data.local.entity.CachedProductEntity

@Dao
interface CachedProductDao {

    @Query("SELECT * FROM cached_products WHERE barcode = :barcode LIMIT 1")
    suspend fun getByBarcode(barcode: String): CachedProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CachedProductEntity)

    @Query("DELETE FROM cached_products WHERE cachedAt < :thresholdMillis")
    suspend fun deleteOlderThan(thresholdMillis: Long)
}
