package pe.ecoscan.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pe.ecoscan.app.data.local.EcoScanDatabase
import pe.ecoscan.app.data.local.MIGRATION_1_2
import pe.ecoscan.app.data.local.MIGRATION_2_3
import pe.ecoscan.app.data.local.MIGRATION_3_4
import pe.ecoscan.app.data.local.dao.CachedProductDao
import pe.ecoscan.app.data.local.dao.UserProfileDao
import pe.ecoscan.app.data.local.dao.WasteRecordDao
import javax.inject.Singleton

private const val DATABASE_NAME = "ecoscan.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideEcoScanDatabase(
        @ApplicationContext context: Context
    ): EcoScanDatabase = Room.databaseBuilder(context, EcoScanDatabase::class.java, DATABASE_NAME)
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        .build()

    @Provides
    fun provideWasteRecordDao(database: EcoScanDatabase): WasteRecordDao = database.wasteRecordDao()

    @Provides
    fun provideUserProfileDao(database: EcoScanDatabase): UserProfileDao = database.userProfileDao()

    @Provides
    fun provideCachedProductDao(database: EcoScanDatabase): CachedProductDao = database.cachedProductDao()
}