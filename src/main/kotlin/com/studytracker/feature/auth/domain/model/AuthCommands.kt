package com.studytracker.feature.auth.domain.model

import com.studytracker.feature.stats.domain.model.DeviceInfo

data class RegisterCommand(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val device: DeviceInfo,
)

data class LoginCommand(
    val email: String,
    val password: String,
    val device: DeviceInfo,
)
