package com.studytracker.shared.storage

data class StoredObject(val contentType: String, val size: Long)

interface FileStorage {
    fun presignedPutUrl(key: String, ttlSeconds: Long): String
    fun presignedGetUrl(key: String, ttlSeconds: Long): String
    suspend fun delete(key: String)

    suspend fun stat(key: String): StoredObject?
}
