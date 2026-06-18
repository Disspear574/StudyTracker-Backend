package com.studytracker.shared.db

import java.sql.SQLException

fun Throwable.isUniqueViolation(): Boolean {
    var cause: Throwable? = this
    while (cause != null) {
        if (cause is SQLException && cause.sqlState == "23505") return true
        cause = cause.cause
    }
    return false
}
