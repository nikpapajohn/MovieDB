package com.nikpapajohn.moviedb.data.local.favorites

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.nikpapajohn.moviedb.data.local.crypto.CryptoManager
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

/**
 * DataStore handles atomic writes and read/write coordination; this serializer only
 * adds the crypto layer, so neither concern leaks into the other.
 */
class EncryptedFavoritesSerializer(
    private val crypto: CryptoManager,
    // Deliberately not the Json singleton from NetworkModule: that one is tuned for
    // reading TMDB's payloads (explicitNulls, coerceInputValues), and this one defines an
    // on-disk format. Sharing it would let a change made for the API rewrite stored files.
    private val json: Json = Json { ignoreUnknownKeys = true },
) : Serializer<FavoritesData> {

    override val defaultValue: FavoritesData = FavoritesData()

    override suspend fun readFrom(input: InputStream): FavoritesData {
        val bytes = input.readBytes()
        if (bytes.isEmpty()) return defaultValue
        return try {
            json.decodeFromString(FavoritesData.serializer(), crypto.decrypt(bytes).decodeToString())
        } catch (e: CancellationException) {
            // Never a CorruptionException: that hands the file to the ReplaceFileCorruption-
            // Handler, which empties it. A cancelled read is not a damaged file, and must
            // not cost the user their favorites.
            throw e
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
