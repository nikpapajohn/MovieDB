package com.nikpapajohn.moviedb.domain.model

// These used to carry @Immutable so that the Compose compiler would treat them as stable
// despite their List fields. That put an androidx dependency in the one layer that is meant
// to have none. compose_stability.conf at the repo root now says the same thing from the
// outside, leaving these as plain Kotlin.

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
