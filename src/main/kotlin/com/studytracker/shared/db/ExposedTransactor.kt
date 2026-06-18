package com.studytracker.shared.db

import com.studytracker.shared.ports.Transactor

class ExposedTransactor : Transactor {
    override suspend fun <T> transaction(block: suspend () -> T): T = dbQuery { block() }
}
