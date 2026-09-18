package com.nikpapajohn.moviedb.data.local.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AES-256/GCM with a key that is generated inside the AndroidKeyStore and never leaves it:
 * the app hands bytes to the system and gets bytes back, it never holds key material.
 *
 * On-disk layout:  [1 byte version][12 byte IV][ciphertext || 16 byte GCM tag]
 *
 * The IV is stored alongside because GCM needs a unique IV per encryption and
 * setRandomizedEncryptionRequired(true) makes the system generate one for every call.
 * GCM also authenticates: a modified file fails to decrypt instead of returning garbage.
 */
@Singleton
class KeystoreCryptoManager @Inject constructor() : CryptoManager {

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }

    override fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, secretKey()) }
        val iv = cipher.iv
        require(iv.size == IV_SIZE) { "Unexpected IV size: ${iv.size}" }
        val cipherText = cipher.doFinal(plain)
        return ByteArray(1 + IV_SIZE + cipherText.size).also { out ->
            out[0] = FORMAT_VERSION
            iv.copyInto(out, destinationOffset = 1)
            cipherText.copyInto(out, destinationOffset = 1 + IV_SIZE)
        }
    }

    override fun decrypt(payload: ByteArray): ByteArray {
        if (payload.size <= 1 + IV_SIZE) {
            throw CryptoUnavailableException("Encrypted payload is too short (${payload.size} bytes)")
        }
        if (payload[0] != FORMAT_VERSION) {
            throw CryptoUnavailableException("Unsupported payload version ${payload[0]}")
        }
        val iv = payload.copyOfRange(1, 1 + IV_SIZE)
        val cipherText = payload.copyOfRange(1 + IV_SIZE, payload.size)
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_SIZE_BITS, iv))
            }
            cipher.doFinal(cipherText)
        } catch (e: KeyPermanentlyInvalidatedException) {
            // Device restored to other hardware, or the lock screen was reset.
            recreateKey()
            throw CryptoUnavailableException("Keystore key was invalidated; favorites were reset", e)
        } catch (e: Exception) {
            throw CryptoUnavailableException("Cannot decrypt favorites", e)
        }
    }

    private fun secretKey(): SecretKey =
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey ?: generateKey()

    private fun generateKey(): SecretKey {
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_SIZE_BITS)
                .setRandomizedEncryptionRequired(true)
                // Deliberately no setUserAuthenticationRequired: favorites must be readable
                // while the screen is locked, and the data is not authentication material.
                .build(),
        )
        return generator.generateKey()
    }

    private fun recreateKey() {
        runCatching { keyStore.deleteEntry(KEY_ALIAS) }
        runCatching { generateKey() }
    }

    private companion object {
        const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        const val KEY_ALIAS = "moviedb_favorites_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_SIZE_BITS = 256
        const val TAG_SIZE_BITS = 128
        const val IV_SIZE = 12
        const val FORMAT_VERSION: Byte = 1
    }
}
