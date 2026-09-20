package com.nikpapajohn.moviedb.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nikpapajohn.moviedb.data.local.crypto.CryptoUnavailableException
import com.nikpapajohn.moviedb.data.local.crypto.KeystoreCryptoManager
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented, not local: the AndroidKeyStore only exists on a device or emulator.
 */
@RunWith(AndroidJUnit4::class)
class KeystoreCryptoManagerTest {

    private val crypto = KeystoreCryptoManager()

    @Test
    fun encrypt_then_decrypt_returns_the_original_bytes() {
        val plain =
            """{"movies":[{"id":278,"title":"The Shawshank Redemption"}]}""".encodeToByteArray()

        val restored = crypto.decrypt(crypto.encrypt(plain))

        assertArrayEquals(plain, restored)
    }

    @Test
    fun the_ciphertext_does_not_contain_the_plaintext() {
        val plain = "The Shawshank Redemption".encodeToByteArray()

        val encrypted = crypto.encrypt(plain).decodeToString()

        assertFalse(encrypted.contains("Shawshank"))
    }

    @Test
    fun every_encryption_uses_a_fresh_iv() {
        val plain = "same input".encodeToByteArray()

        val first = crypto.encrypt(plain)
        val second = crypto.encrypt(plain)

        val firstIv = first.copyOfRange(1, 13).toList()
        val secondIv = second.copyOfRange(1, 13).toList()
        assertNotEquals(firstIv, secondIv)
        assertNotEquals(first.toList(), second.toList())
    }

    @Test
    fun a_tampered_payload_fails_to_decrypt() {
        val encrypted = crypto.encrypt("favorites".encodeToByteArray())
        encrypted[encrypted.size - 1] = (encrypted[encrypted.size - 1] + 1).toByte()

        val thrown = runCatching { crypto.decrypt(encrypted) }.exceptionOrNull()

        assertEquals(CryptoUnavailableException::class.java, thrown?.javaClass)
    }

    @Test
    fun a_truncated_payload_is_rejected() {
        val thrown = runCatching { crypto.decrypt(ByteArray(4)) }.exceptionOrNull()

        assertEquals(CryptoUnavailableException::class.java, thrown?.javaClass)
    }
}
