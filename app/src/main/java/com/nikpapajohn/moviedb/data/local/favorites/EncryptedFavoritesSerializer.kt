package com.nikpapajohn.moviedb.data.local.favorites

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.nikpapajohn.moviedb.data.local.crypto.CryptoManager
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.json.Json

/**
 * DataStore handles atomic writes and read/write coordination; this serializer only
 * adds the crypto layer, so neither concern leaks into the other.
 */
class EncryptedFavoritesSerializer(
    private val crypto: CryptoManager,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : Serializer<FavoritesData> {

    override val defaultValue: FavoritesData = FavoritesData()

    override suspend fun readFrom(input: InputStream): FavoritesData {
        val bytes = input.readBytes()
        if (bytes.isEmpty()) return defaultValue
        return try {
            json.decodeFromString(FavoritesData.serializer(), crypto.decrypt(bytes).decodeToString())
        } catch (e: Exception) {
            // Handled by the ReplaceFileCorruptionHandler: the user loses favorites, the app does not crash.
            throw CorruptionException("Favorites could not be read", e)
        }
    }

    override suspend fun writeTo(t: FavoritesData, output: OutputStream) {
        val plain = json.encodeToString(FavoritesData.serializer(), t).encodeToByteArray()
        output.write(crypto.encrypt(plain))
    }
}
