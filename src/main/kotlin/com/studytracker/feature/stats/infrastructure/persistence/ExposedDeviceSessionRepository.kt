package com.studytracker.feature.stats.infrastructure.persistence

import com.studytracker.feature.stats.domain.model.DeviceInfo
import com.studytracker.feature.stats.domain.port.DeviceSessionRepository
import com.studytracker.shared.db.dbQuery
import com.studytracker.shared.ports.UuidV7
import org.jetbrains.exposed.v1.jdbc.upsert
import java.time.LocalDateTime
import java.util.UUID

class ExposedDeviceSessionRepository : DeviceSessionRepository {
    override suspend fun record(userId: UUID, info: DeviceInfo, now: LocalDateTime) {
        dbQuery {
            DeviceSessionTable.upsert(
                DeviceSessionTable.userId,
                DeviceSessionTable.deviceUuid,
                onUpdateExclude = listOf(DeviceSessionTable.id, DeviceSessionTable.createdAt),
            ) {
                it[id] = UuidV7.next()
                it[DeviceSessionTable.userId] = userId
                it[deviceUuid] = info.deviceUuid
                it[os] = info.os
                it[osVersion] = info.osVersion
                it[appVersion] = info.appVersion
                it[deviceModel] = info.deviceModel
                it[createdAt] = now
                it[lastSeenAt] = now
            }
        }
    }
}
