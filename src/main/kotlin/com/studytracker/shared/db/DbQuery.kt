package com.studytracker.shared.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

suspend fun <T> dbQuery(block: suspend JdbcTransaction.() -> T): T =
    withContext(Dispatchers.IO) {
        suspendTransaction { block() }
    }
