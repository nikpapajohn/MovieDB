package com.nikpapajohn.moviedb.data.local.favorites

import com.nikpapajohn.moviedb.domain.model.Movie
import kotlinx.serialization.Serializable

/**
 * A snapshot of what the user favorited, not just the id: the favorites screen has to
 * render with no network, and TMDB ids alone would need a round trip per movie.
 */
@Serializable
data class FavoriteMovie(
    val id: Int,
    val title: String,
    val posterPath: String? = null,
    val rating: Double = 0.0,
    val genreNames: List<String> = emptyList(),
    val addedAtEpochMillis: Long = 0L,
)

@Serializable
data class FavoritesData(
    val movies: List<FavoriteMovie> = emptyList(),
)

fun FavoriteMovie.toDomain(): Movie = Movie(
    id = id,
    title = title,
    posterPath = posterPath,
    rating = rating,
    genreNames = genreNames,
)

fun Movie.toFavorite(now: Long): FavoriteMovie = FavoriteMovie(
    id = id,
    title = title,
    posterPath = posterPath,
    rating = rating,
    genreNames = genreNames,
    addedAtEpochMillis = now,
)
