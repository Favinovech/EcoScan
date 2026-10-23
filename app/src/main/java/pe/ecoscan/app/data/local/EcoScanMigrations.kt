package pe.ecoscan.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Versión 1 -> 2: agrega la tabla del perfil de usuario (HU03).
// El SQL debe coincidir exactamente con el que Room genera para UserProfileEntity
// (puedes compararlo con app/schemas/.../2.json después de compilar).
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `user_profiles` (" +
                    "`uid` TEXT NOT NULL, " +
                    "`email` TEXT NOT NULL, " +
                    "`displayName` TEXT NOT NULL, " +
                    "`district` TEXT NOT NULL, " +
                    "`photoUrl` TEXT, " +
                    "`notificationsEnabled` INTEGER NOT NULL, " +
                    "`weeklyGoalKg` INTEGER NOT NULL, " +
                    "`isSynced` INTEGER NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY(`uid`))"
        )
    }
}

// Versión 2 -> 3: agrega la tabla de caché de productos consultados a Open Food Facts (HU05).
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `cached_products` (" +
                    "`barcode` TEXT NOT NULL, " +
                    "`name` TEXT, " +
                    "`brand` TEXT, " +
                    "`packagingMaterials` TEXT NOT NULL, " +
                    "`imageUrl` TEXT, " +
                    "`disposalHint` TEXT NOT NULL, " +
                    "`cachedAt` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`barcode`))"
        )
    }
}

// Versión 3 -> 4: prepara waste_records para el historial por usuario y la sincronización
// con Firestore (HU06): dueño del registro, ID estable para Firestore, eliminación
// pendiente y fecha de última modificación. Los registros que ya existían conservan sus
// datos: reciben un syncId aleatorio y updatedAt = recordedAt.
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `waste_records` ADD COLUMN `userId` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `waste_records` ADD COLUMN `syncId` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `waste_records` ADD COLUMN `isDeleted` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `waste_records` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0")
        db.execSQL(
            "UPDATE `waste_records` SET `syncId` = lower(hex(randomblob(16))), `updatedAt` = `recordedAt`"
        )
    }
}

// Versión 4 -> 5: agrega la tabla de caché de puntos de acopio para el mapa sin conexión (HU07).
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `cached_collection_points` (" +
                    "`id` TEXT NOT NULL, " +
                    "`name` TEXT, " +
                    "`latitude` REAL NOT NULL, " +
                    "`longitude` REAL NOT NULL, " +
                    "`address` TEXT, " +
                    "`openingHours` TEXT, " +
                    "`acceptedMaterials` TEXT NOT NULL, " +
                    "`cachedAt` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`id`))"
        )
    }
}