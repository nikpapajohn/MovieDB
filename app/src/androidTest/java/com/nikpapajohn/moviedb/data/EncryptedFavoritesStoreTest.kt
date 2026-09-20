package com.nikpapajohn.moviedb.data

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nikpapajohn.moviedb.data.local.crypto.KeystoreCryptoManager
import com.nikpapajohn.moviedb.data.local.favorites.EncryptedFavoritesSerializer
import com.nikpapajohn.moviedb.data.local.favorites.FavoritesData
import com.nikpapajohn.moviedb.data.repository.FavoritesRepositoryImpl
import com.nikpapajohn.moviedb.domain.model.Movie
import com.nikpapajohn.moviedb.domain.model.MovieDetails
import com.nikpapajohn.moviedb.domain.model.MoviePage
import com.nikpapajohn.moviedb.domain.repository.MovieRepository
import com.nikpapajohn.moviedb.util.InstrumentedDispatcherProvider
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EncryptedFavoritesStoreTest {

    private lateinit var file: File

    /**
     * DataStore allows exactly one active instance per file, by design: two instances would
     * overwrite each other. So a test that simulates "close the app and reopen it" must
     * cancel the previous store's scope and wait for it, not just create a second store.
     */
    private val scopes = mutableListOf<CoroutineScope>()

    private val movie = Movie(
        id = 278,
        title = "The Shawshank Redemption",
        posterPath = "/poster.jpg",
        rating = 8.7,
        genreNames = listOf("Drama")
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        file = File(context.filesDir, "favorites_test_${System.nanoTime()}.enc")
    }

    @After
    fun tearDown() {
        runBlocking { closeStores() }
        file.delete()
    }

    private fun openStore(): DataStore<FavoritesData> {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scopes += scope
        return DataStoreFactory.create(
            serializer = EncryptedFavoritesSerializer(KeystoreCryptoManager()),
            corruptionHandler = ReplaceFileCorruptionHandler { FavoritesData() },
            scope = scope,
            produceFile = { file }
        )
    }

    /** Cancels every store's scope and waits for it, which releases the file. */
    private suspend fun closeStores() {
        scopes.forEach { it.coroutineContext.job.cancelAndJoin() }
        scopes.clear()
    }

    private fun repository(store: DataStore<FavoritesData>) =
        FavoritesRepositoryImpl(store,
            FakeMovieRepository(), InstrumentedDispatcherProvider())

    /**
     * None of these tests exercise [FavoritesRepositoryImpl.refresh], the only method that
     * calls through to [MovieRepository] — so a fake that is never invoked is enough here.
     */
    private class FakeMovieRepository : MovieRepository {
        override suspend fun popularMovies(page: Int): Result<MoviePage> =
            Result.failure(UnsupportedOperationException())

        override suspend fun searchMovies(query: String, page: Int): Result<MoviePage> =
            Result.failure(UnsupportedOperationException())

        override suspend fun movieDetails(movieId: Int): Result<MovieDetails> =
            Result.failure(UnsupportedOperationException())
    }

    @Test
    fun toggle_adds_then_removes_the_movie() = runTest {
        val repository = repository(openStore())

        assertTrue(repository.toggle(movie))
        assertEquals(setOf(278), repository.favoriteIds().first())

        assertFalse(repository.toggle(movie))
        assertTrue(repository.favoriteIds().first().isEmpty())
    }

    @Test
    fun the_stored_file_never_holds_the_title_in_clear_text() = runTest {
        repository(openStore()).toggle(movie)

        val onDisk = file.readBytes().decodeToString()

        assertFalse(onDisk.contains("Shawshank"))
        assertFalse(onDisk.contains("poster.jpg"))
    }

    @Test
    fun favorites_survive_a_restart() = runTest {
        repository(openStore()).toggle(movie)

        closeStores()
        val reopened = openStore()

        assertEquals(listOf(278), reopened.data.first().movies.map { it.id })
    }

    @Test
    fun a_corrupted_file_resets_to_empty_instead_of_crashing() = runTest {
        repository(openStore()).toggle(movie)
        closeStores()

        file.writeBytes("garbage that is not a valid payload".encodeToByteArray())
        val reopened = openStore()

        assertTrue(reopened.data.first().movies.isEmpty())
    }
}
