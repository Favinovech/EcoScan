package pe.ecoscan.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pe.ecoscan.app.data.local.converter.WasteCategoryConverter
import pe.ecoscan.app.data.local.dao.WasteRecordDao
import pe.ecoscan.app.data.local.entity.WasteRecordEntity

@Database(entities = [WasteRecordEntity::class], version = 1, exportSchema = true)
@TypeConverters(WasteCategoryConverter::class)
abstract class EcoScanDatabase : RoomDatabase() {
    abstract fun wasteRecordDao(): WasteRecordDao
}
