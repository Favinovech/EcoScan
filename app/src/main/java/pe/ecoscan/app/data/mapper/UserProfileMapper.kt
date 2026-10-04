package pe.ecoscan.app.data.mapper

import pe.ecoscan.app.data.local.entity.UserProfileEntity
import pe.ecoscan.app.domain.model.UserProfile

fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
    uid = uid,
    email = email,
    displayName = displayName,
    district = district,
    photoUrl = photoUrl,
    notificationsEnabled = notificationsEnabled,
    weeklyGoalKg = weeklyGoalKg,
    isSynced = isSynced
)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
    uid = uid,
    email = email,
    displayName = displayName,
    district = district,
    photoUrl = photoUrl,
    notificationsEnabled = notificationsEnabled,
    weeklyGoalKg = weeklyGoalKg,
    isSynced = isSynced
)

// Documento de Firestore: users/{uid}
fun UserProfile.toFirestoreMap(): Map<String, Any?> = mapOf(
    "email" to email,
    "displayName" to displayName,
    "district" to district,
    "photoUrl" to photoUrl,
    "notificationsEnabled" to notificationsEnabled,
    "weeklyGoalKg" to weeklyGoalKg,
    "updatedAt" to System.currentTimeMillis()
)