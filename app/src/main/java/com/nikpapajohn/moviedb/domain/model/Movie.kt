package com.nikpapajohn.moviedb.domain.model

import androidx.compose.runtime.Immutable

// @Immutable is a Compose-runtime annotation living in the domain layer, which otherwise
// knows nothing about UI. Trade-off accepted deliberately: without it, the List field below
// makes the compiler treat this class as unstable, and every MovieCard/MovieList composable
// that takes one falls back to reference-identity skipping instead of real equality checks.

/** A movie as the list screen needs it. Nothing here knows about TMDB or Retrofit. */
@Immutable
data class Movie(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val rating: Double,
    val genreNames: List<String> = emptyList(),
)

/** A movie plus the only piece of state that is ours, not TMDB's. */
@Immutable
data class MovieListItem(
    val movie: Movie,
    val isFavorite: Boolean,
)
