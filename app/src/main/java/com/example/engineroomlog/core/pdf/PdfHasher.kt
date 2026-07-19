package com.example.engineroomlog.core.pdf

import java.io.File
import java.security.MessageDigest

// SHA-256 of a file, hex-encoded. The fingerprint that makes
// "this journal page was never altered" a provable claim, not a promise.
object PdfHasher {

    fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { stream ->
            val buffer = ByteArray(8192)
            while (true) {
                val read = stream.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}