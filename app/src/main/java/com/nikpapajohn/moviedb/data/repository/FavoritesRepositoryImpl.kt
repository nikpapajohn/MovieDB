package com.nikpapajohn.moviedb.data.repository

import androidx.datastore.core.DataStore
import com.nikpapajohn.moviedb.core.DispatcherProvider
import com.nikpapajohn.moviedb.data.local.favorites.FavoritesData
import com.nikpapajohn.moviedb.data.local.favorites.toDomain
import com.nikpapajohn.moviedb.data.local.favorites.toFavorite
import com.nikpapajohn.moviedb.domain.model.Movie
import com.nikpapajohn.moviedb.domain.repository.FavoritesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<FavoritesData>,
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

    override suspend fun toggle(movie: Movie): Boolean = withContext(dispatchers.io) {
        val wasFavorite = dataStore.data.first().movies.any { it.id == movie.id }
        dataStore.updateData { current ->
            val without = current.movies.filterNot { it.id == movie.id }
            if (wasFavorite) {
                current.copy(movies = without)
            } else {
                current.copy(movies = without + movie.toFavorite(System.currentTimeMillis()))
            }
        }
        !wasFavorite
    }

    override suspend fun clear() = withContext(dispatchers.io) {
        dataStore.updateData { FavoritesData() }
        Unit
    }
}
