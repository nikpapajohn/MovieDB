package com.nikpapajohn.moviedb.data.repository

import com.nikpapajohn.moviedb.core.safeApiCall
import com.nikpapajohn.moviedb.data.mapper.toDomain
import com.nikpapajohn.moviedb.data.remote.GenreCache
import com.nikpapajohn.moviedb.data.remote.TmdbApi
import com.nikpapajohn.moviedb.data.remote.dto.MoviePageDto
import com.nikpapajohn.moviedb.domain.model.MovieDetails
import com.nikpapajohn.moviedb.domain.model.MoviePage
import com.nikpapajohn.moviedb.domain.repository.MovieRepository
import javax.inject.Inject
import javax.inject.Singleton

// No withContext(dispatchers.io) here: Retrofit's suspend support is already main-safe —
// the call and its response parsing run on OkHttp's own dispatcher, not the caller's — so
// wrapping it would only add indirection with nothing to protect against.
@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val api: TmdbApi,
    private val genreCache: GenreCache
) : MovieRepository {

    override suspend fun popularMovies(page: Int): Result<MoviePage> =
        safeApiCall { api.popularMovies(page = page).toMoviePage() }

    override suspend fun searchMovies(query: String, page: Int): Result<MoviePage> =
        safeApiCall { api.searchMovies(query = query, page = page).toMoviePage() }

    override suspend fun movieDetails(movieId: Int): Result<MovieDetails> =
        safeApiCall { api.movieDetails(movieId = movieId).toDomain() }

    private suspend fun MoviePageDto.toMoviePage() = MoviePage(
        page = page,
        movies = results.map { dto -> dto.toDomain(genreCache.namesFor(dto.genreIds)) },
        totalPages = totalPages
    )
}
