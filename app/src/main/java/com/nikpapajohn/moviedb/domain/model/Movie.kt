package com.nikpapajohn.moviedb.domain.model

/** A movie as the list screen needs it. Nothing here knows about TMDB or Retrofit. */
data class Movie(
    val id: Int,
    val title: String,
    val posterPath: String?,
    val rating: Double,
    val genreNames: List<String> = emptyList(),
)

/** A movie plus the only piece of state that is ours, not TMDB's. */
data class MovieListItem(
    val movie: Movie,
    val isFavorite: Boolean,
)
