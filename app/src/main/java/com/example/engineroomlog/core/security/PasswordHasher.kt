package com.example.engineroomlog.core.security

import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordHasher {

    // Hash a plaintext password for storage. A random salt is embedded in the result.
    fun hash(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    // Verify a plaintext password against a stored hash.
    fun verify(password: String, storedHash: String): Boolean {
        val result = BCrypt.verifyer().verify(password.toCharArray(), storedHash)
        return result.verified
    }
}