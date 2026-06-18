package com.studytracker.shared.security

import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordHasher {
    private const val COST = 12

    fun hash(raw: String): String =
        BCrypt.withDefaults().hashToString(COST, raw.toCharArray())

    fun verify(raw: String, hash: String): Boolean =
        BCrypt.verifyer().verify(raw.toCharArray(), hash).verified
}
