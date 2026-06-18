package com.studytracker.feature.stats.domain.model

data class DeviceInfo(
    val deviceUuid: String,
    val os: OsType,
    val osVersion: String,
    val appVersion: String,
    val deviceModel: String?,
)
