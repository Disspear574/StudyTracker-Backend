package com.studytracker.shared.ports

import java.time.LocalDateTime
import java.time.ZoneOffset

fun interface TimeProvider {
    fun now(): LocalDateTime
}

class SystemTimeProvider : TimeProvider {
    override fun now(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)
}
