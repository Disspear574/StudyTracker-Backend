package com.studytracker.feature.user.infrastructure.storage

import com.studytracker.shared.storage.FileStorage
import com.studytracker.shared.storage.StoredObject
import io.minio.BucketExistsArgs
import io.minio.GetPresignedObjectUrlArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.RemoveObjectArgs
import io.minio.StatObjectArgs
import io.minio.errors.ErrorResponseException
import io.minio.http.Method
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MinioFileStorage(
    private val client: MinioClient,
    private val bucket: String,
) : FileStorage {

    fun ensureBucket() {
        val exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build())
        }
    }

    override fun presignedPutUrl(key: String, ttlSeconds: Long): String =
        client.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(bucket)
                .`object`(key)
                .expiry(ttlSeconds.toInt())
                .build(),
        )

    override fun presignedGetUrl(key: String, ttlSeconds: Long): String =
        client.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(bucket)
                .`object`(key)
                .expiry(ttlSeconds.toInt())
                .build(),
        )

    override suspend fun delete(key: String) {
        withContext(Dispatchers.IO) {
            client.removeObject(RemoveObjectArgs.builder().bucket(bucket).`object`(key).build())
        }
    }

    override suspend fun stat(key: String): StoredObject? = withContext(Dispatchers.IO) {
        try {
            val stat = client.statObject(StatObjectArgs.builder().bucket(bucket).`object`(key).build())
            StoredObject(contentType = stat.contentType(), size = stat.size())
        } catch (e: ErrorResponseException) {
            if (e.errorResponse()?.code() == "NoSuchKey") null else throw e
        }
    }
}

