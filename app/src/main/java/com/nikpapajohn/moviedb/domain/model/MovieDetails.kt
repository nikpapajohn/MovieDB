package com.nikpapajohn.moviedb.domain.model

data class Genre(
    val id: Int,
    val name: String,
)

data class MovieDetails(
    val id: Int,
    val title: String,
    val tagline: String?,
    val overview: String,
    val posterPath: String?,
    val rating: Double,
    val voteCount: Int,
    /** ISO date as TMDB sends it; may be null or blank for unreleased entries. */
    val releaseDate: String?,
    val runtimeMinutes: Int?,
    val genres: List<Genre>,
) {
    val releaseYear: String? = releaseDate?.takeIf { it.length >= 4 }?.substring(0, 4)

    fun toMovie(): Movie = Movie(
        id = id,
        title = title,
        posterPath = posterPath,
        rating = rating,
        genreNames = genres.map { it.name },
    )
}
