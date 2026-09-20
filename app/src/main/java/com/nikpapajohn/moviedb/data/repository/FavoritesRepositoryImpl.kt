package com.nikpapajohn.moviedb.data.repository

import androidx.datastore.core.DataStore
import com.nikpapajohn.moviedb.core.DispatcherProvider
import com.nikpapajohn.moviedb.data.local.favorites.FavoritesData
import com.nikpapajohn.moviedb.data.local.favorites.toDomain
import com.nikpapajohn.moviedb.data.local.favorites.toFavorite
import com.nikpapajohn.moviedb.domain.model.Movie
import com.nikpapajohn.moviedb.domain.repository.FavoritesRepository
import com.nikpapajohn.moviedb.domain.repository.MovieRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<FavoritesData>,
    private val movieRepository: MovieRepository,
    private val dispatchers: DispatcherProvider,
) : FavoritesRepository {

    override fun favoriteIds(): Flow<Set<Int>> =
        dataStore.data.map { data -> data.movies.map { it.id }.toSet() }.distinctUntilChanged()

    override fun favorites(): Flow<List<Movie>> =
        dataStore.data.map { data ->
            data.movies.sortedByDescending { it.addedAtEpochMillis }.map { it.toDomain() }
        }

    override fun isFavorite(movieId: Int): Flow<Boolean> =
        dataStore.data.map { data -> data.movies.any { it.id == movieId } }.distinctUntilChanged()

    // "Was it already there?" is decided inside updateData, on that transaction's own
    // snapshot. Reading it first would be a separate transaction: two toggles racing (a
    // double tap, or the list and the details screen at once) would both see "not a
    // favorite", both add, and both report that they added it. It also costs one decryption
    // instead of two, since updateData already hands back the state it wrote.
    override suspend fun toggle(movie: Movie): Boolean = withContext(dispatchers.io) {
        val updated = dataStore.updateData { current ->
            val without = current.movies.filterNot { it.id == movie.id }
            val wasFavorite = without.size != current.movies.size
            if (wasFavorite) {
                current.copy(movies = without)
            } else {
                current.copy(movies = without + movie.toFavorite(System.currentTimeMillis()))
            }
        }
        updated.movies.any { it.id == movie.id }
    }

    override suspend fun clear() = withContext(dispatchers.io) {
        dataStore.updateData { FavoritesData() }
        Unit
    }

    override suspend fun refresh() = withContext(dispatchers.io) {
        val current = dataStore.data.first().movies
        if (current.isEmpty()) return@withContext

        // Parallel, not sequential: a dozen favorites should not mean a dozen round trips
        // back to back. A movie whose call fails just keeps its old cached snapshot.
        val refreshed = current.map { favorite ->
            async {
                movieRepository.movieDetails(favorite.id)
                    .map { details -> details.toMovie().toFavorite(favorite.addedAtEpochMillis) }
                    .getOrDefault(favorite)
            }
        }.awaitAll()

        dataStore.updateData { it.copy(movies = refreshed) }
        Unit
    }
}
