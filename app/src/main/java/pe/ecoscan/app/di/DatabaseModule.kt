package pe.ecoscan.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pe.ecoscan.app.data.local.EcoScanDatabase
import pe.ecoscan.app.data.local.dao.WasteRecordDao
import pe.ecoscan.app.data.local.entity.WasteRecordEntity
import pe.ecoscan.app.domain.model.WasteCategory
import javax.inject.Provider
import javax.inject.Singleton

private const val DATABASE_NAME = "ecoscan.db"

// Callback que precarga 8 registros de ejemplo la primera vez que se crea la base de
// datos, para que el historial no arranque vacío. Es data de desarrollo: se elimina
// cuando exista el flujo real de escaneo que inserte registros.
class EcoScanDatabaseCallback(
    private val databaseProvider: Provider<EcoScanDatabase>
) : RoomDatabase.Callback() {

    private val callbackScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        callbackScope.launch {
            databaseProvider.get().wasteRecordDao().insertAll(seedRecords())
        }
    }

    private fun seedRecords(): List<WasteRecordEntity> {
        val dayMillis = 24 * 60 * 60 * 1000L
        val now = System.currentTimeMillis()
        return listOf(
            WasteRecordEntity(category = WasteCategory.PLASTIC, weightKg = 2.5, points = 25, recordedAt = now - 0 * dayMillis),
            WasteRecordEntity(category = WasteCategory.GLASS, weightKg = 5.0, points = 30, recordedAt = now - 1 * dayMillis),
            WasteRecordEntity(category = WasteCategory.PAPER, weightKg = 3.2, points = 16, recordedAt = now - 2 * dayMillis),
            WasteRecordEntity(category = WasteCategory.METAL, weightKg = 1.8, points = 36, recordedAt = now - 3 * dayMillis),
            WasteRecordEntity(category = WasteCategory.ORGANIC, weightKg = 4.5, points = 9, recordedAt = now - 4 * dayMillis),
            WasteRecordEntity(category = WasteCategory.HAZARDOUS, weightKg = 0.5, points = 50, recordedAt = now - 5 * dayMillis),
            WasteRecordEntity(category = WasteCategory.PLASTIC, weightKg = 1.2, points = 12, recordedAt = now - 6 * dayMillis),
            WasteRecordEntity(category = WasteCategory.PAPER, weightKg = 6.0, points = 30, recordedAt = now - 7 * dayMillis)
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideEcoScanDatabase(
        @ApplicationContext context: Context,
        databaseProvider: Provider<EcoScanDatabase>
    ): EcoScanDatabase = Room.databaseBuilder(context, EcoScanDatabase::class.java, DATABASE_NAME)
        .addCallback(EcoScanDatabaseCallback(databaseProvider))
        .build()

    @Provides
    fun provideWasteRecordDao(database: EcoScanDatabase): WasteRecordDao = database.wasteRecordDao()
}
