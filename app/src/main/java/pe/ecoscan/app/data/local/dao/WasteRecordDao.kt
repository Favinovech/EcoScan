package pe.ecoscan.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pe.ecoscan.app.data.local.entity.WasteRecordEntity
import pe.ecoscan.app.domain.model.WasteCategory

@Dao
interface WasteRecordDao {

    @Query("SELECT * FROM waste_records ORDER BY recordedAt DESC")
    fun getAll(): Flow<List<WasteRecordEntity>>

    @Query("SELECT COALESCE(SUM(points), 0) FROM waste_records")
    fun getTotalPoints(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: WasteRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<WasteRecordEntity>)

    @Query("DELETE FROM waste_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM waste_records")
    suspend fun deleteAll()

    // ---- HU06: historial por usuario y sincronización ----

    // Registros visibles del usuario (sin los marcados como eliminados).
    @Query("SELECT * FROM waste_records WHERE userId = :uid AND isDeleted = 0 ORDER BY recordedAt DESC")
    fun observeByUser(uid: String): Flow<List<WasteRecordEntity>>

    @Query("SELECT * FROM waste_records WHERE userId = :uid")
    suspend fun getAllByUser(uid: String): List<WasteRecordEntity>

    // Cambios locales que faltan por subir (altas, ediciones y eliminaciones).
    @Query("SELECT * FROM waste_records WHERE userId = :uid AND isSynced = 0")
    suspend fun getPendingByUser(uid: String): List<WasteRecordEntity>

    @Query("SELECT COUNT(*) FROM waste_records WHERE userId = :uid AND isSynced = 0")
    fun observePendingCount(uid: String): Flow<Int>

    // Registros creados antes de HU06 no tienen dueño: los adopta el usuario que inicia sesión.
    @Query("UPDATE waste_records SET userId = :uid WHERE userId = ''")
    suspend fun claimLegacyRecords(uid: String)

    @Query(
        "UPDATE waste_records SET category = :category, weightKg = :weightKg, points = :points, " +
                "updatedAt = :updatedAt, isSynced = 0 WHERE id = :id AND isDeleted = 0"
    )
    suspend fun updateEditable(
        id: Long,
        category: WasteCategory,
        weightKg: Double,
        points: Int,
        updatedAt: Long
    ): Int

    @Query("UPDATE waste_records SET isDeleted = 1, isSynced = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markDeleted(id: Long, updatedAt: Long): Int

    // Solo marca como sincronizado si el registro no cambió mientras se subía.
    @Query("UPDATE waste_records SET isSynced = 1 WHERE id = :id AND updatedAt = :updatedAt")
    suspend fun markSyncedIfUnchanged(id: Long, updatedAt: Long)

    @Query(
        "UPDATE waste_records SET category = :category, weightKg = :weightKg, points = :points, " +
                "recordedAt = :recordedAt, updatedAt = :updatedAt, isSynced = 1 WHERE id = :id"
    )
    suspend fun applyRemote(
        id: Long,
        category: WasteCategory,
        weightKg: Double,
        points: Int,
        recordedAt: Long,
        updatedAt: Long
    )
}