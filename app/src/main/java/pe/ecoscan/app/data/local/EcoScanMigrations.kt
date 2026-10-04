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