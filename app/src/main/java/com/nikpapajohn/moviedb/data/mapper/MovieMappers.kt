package com.nikpapajohn.moviedb.data.mapper

import com.nikpapajohn.moviedb.data.remote.dto.GenreDto
import com.nikpapajohn.moviedb.data.remote.dto.MovieDetailsDto
import com.nikpapajohn.moviedb.data.remote.dto.MovieDto
import com.nikpapajohn.moviedb.domain.model.Genre
import com.nikpapajohn.moviedb.domain.model.Movie
import com.nikpapajohn.moviedb.domain.model.MovieDetails

fun MovieDto.toDomain(genreNames: List<String> = emptyList()): Movie = Movie(
    id = id,
    title = title?.takeIf { it.isNotBlank() } ?: originalTitle.orEmpty(),
    posterPath = posterPath?.takeIf { it.isNotBlank() },
    rating = voteAverage ?: 0.0,
    genreNames = genreNames,
)

fun GenreDto.toDomain(): Genre = Genre(id = id, name = name)

fun MovieDetailsDto.toDomain(): MovieDetails = MovieDetails(
    id = id,
    title = title.orEmpty(),
    tagline = tagline?.takeIf { it.isNotBlank() },
    overview = overview.orEmpty(),
    posterPath = posterPath?.takeIf { it.isNotBlank() },
    backdropPath = backdropPath?.takeIf { it.isNotBlank() },
    rating = voteAverage ?: 0.0,
    voteCount = voteCount ?: 0,
    releaseDate = releaseDate?.takeIf { it.isNotBlank() },
    runtimeMinutes = runtime?.takeIf { it > 0 },
    genres = genres.orEmpty().map { it.toDomain() },
)
