package com.studytracker.feature.stats.domain.port

import com.studytracker.feature.stats.domain.model.DeviceInfo
import java.time.LocalDateTime
import java.util.UUID

interface DeviceSessionRepository {
    suspend fun record(userId: UUID, info: DeviceInfo, now: LocalDateTime)
}
