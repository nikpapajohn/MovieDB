package com.nikpapajohn.moviedb.domain.repository

import com.nikpapajohn.moviedb.domain.model.MovieDetails
import com.nikpapajohn.moviedb.domain.model.MoviePage

interface MovieRepository {
    suspend fun popularMovies(page: Int): Result<MoviePage>

    suspend fun searchMovies(query: String, page: Int): Result<MoviePage>

    suspend fun movieDetails(movieId: Int): Result<MovieDetails>
}
