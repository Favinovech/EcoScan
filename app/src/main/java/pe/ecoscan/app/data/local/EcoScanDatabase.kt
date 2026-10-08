package pe.ecoscan.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pe.ecoscan.app.data.local.converter.WasteCategoryConverter
import pe.ecoscan.app.data.local.dao.CachedProductDao
import pe.ecoscan.app.data.local.dao.UserProfileDao
import pe.ecoscan.app.data.local.dao.WasteRecordDao
import pe.ecoscan.app.data.local.entity.CachedProductEntity
import pe.ecoscan.app.data.local.entity.UserProfileEntity
import pe.ecoscan.app.data.local.entity.WasteRecordEntity

@Database(
    entities = [WasteRecordEntity::class, UserProfileEntity::class, CachedProductEntity::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(WasteCategoryConverter::class)
abstract class EcoScanDatabase : RoomDatabase() {
    abstract fun wasteRecordDao(): WasteRecordDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun cachedProductDao(): CachedProductDao
}