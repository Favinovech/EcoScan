package pe.ecoscan.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pe.ecoscan.app.data.local.entity.WasteRecordEntity

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
}
