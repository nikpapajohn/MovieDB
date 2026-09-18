package com.nikpapajohn.moviedb.data.local.crypto

interface CryptoManager {
    /** Returns version byte + IV + ciphertext (with the GCM tag appended). */
    fun encrypt(plain: ByteArray): ByteArray

    /** Reverses [encrypt]. Throws when the payload was tampered with or the key is gone. */
    fun decrypt(payload: ByteArray): ByteArray
}

/** The stored blob cannot be read with the current key: wrong format, tampering, or a lost key. */
class CryptoUnavailableException(message: String, cause: Throwable? = null) : Exception(message, cause)
