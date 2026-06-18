package com.studytracker.shared.security

interface PasswordEncoder {
    fun hash(raw: String): String
    fun matches(raw: String, hash: String): Boolean
}

class BcryptPasswordEncoder : PasswordEncoder {
    override fun hash(raw: String): String = PasswordHasher.hash(raw)
    override fun matches(raw: String, hash: String): Boolean = PasswordHasher.verify(raw, hash)
}
