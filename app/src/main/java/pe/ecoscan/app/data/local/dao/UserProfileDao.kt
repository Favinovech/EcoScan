package pe.ecoscan.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pe.ecoscan.app.data.local.entity.UserProfileEntity

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profiles WHERE uid = :uid")
    fun observe(uid: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE uid = :uid")
    suspend fun get(uid: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfileEntity)

    @Query("UPDATE user_profiles SET photoUrl = :photoUrl, isSynced = 1 WHERE uid = :uid")
    suspend fun markSynced(uid: String, photoUrl: String?)

    @Query("DELETE FROM user_profiles WHERE uid = :uid")
    suspend fun deleteByUid(uid: String)
}