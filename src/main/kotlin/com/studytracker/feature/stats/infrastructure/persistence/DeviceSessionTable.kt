package com.studytracker.feature.stats.infrastructure.persistence

import com.studytracker.feature.stats.domain.model.OsType
import com.studytracker.feature.user.infrastructure.persistence.UserTable
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.java.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime

object DeviceSessionTable : UUIDTable("device_sessions") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val deviceUuid = varchar("device_uuid", 128)
    val os = enumerationByName("os", 16, OsType::class)
    val osVersion = varchar("os_version", 32)
    val appVersion = varchar("app_version", 32)
    val deviceModel = varchar("device_model", 128).nullable()
    val createdAt = datetime("created_at")
    val lastSeenAt = datetime("last_seen_at")

    init {
        uniqueIndex(userId, deviceUuid)
    }
}
