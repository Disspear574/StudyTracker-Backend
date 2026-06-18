package com.studytracker.shared.ports

interface Transactor {
    suspend fun <T> transaction(block: suspend () -> T): T
}
