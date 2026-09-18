package com.nikpapajohn.moviedb.domain.repository

import com.nikpapajohn.moviedb.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    /** Ids only: this is what the list screen combines against. */
    fun favoriteIds(): Flow<Set<Int>>

    /** Full snapshots, so the favorites screen works with no network. */
    fun favorites(): Flow<List<Movie>>

    fun isFavorite(movieId: Int): Flow<Boolean>

    /** Adds when absent, removes when present. Returns the state after the toggle. */
    suspend fun toggle(movie: Movie): Boolean

    suspend fun clear()
}
