package com.nikpapajohn.moviedb.data.repository

import com.nikpapajohn.moviedb.core.DispatcherProvider
import com.nikpapajohn.moviedb.core.safeApiCall
import com.nikpapajohn.moviedb.data.mapper.toDomain
import com.nikpapajohn.moviedb.data.remote.GenreCache
import com.nikpapajohn.moviedb.data.remote.TmdbApi
import com.nikpapajohn.moviedb.data.remote.dto.MoviePageDto
import com.nikpapajohn.moviedb.di.TmdbLanguage
import com.nikpapajohn.moviedb.domain.model.MovieDetails
import com.nikpapajohn.moviedb.domain.model.MoviePage
import com.nikpapajohn.moviedb.domain.repository.MovieRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class MovieRepositoryImpl @Inject constructor(
    private val api: TmdbApi,
    private val genreCache: GenreCache,
    private val dispatchers: DispatcherProvider,
    @TmdbLanguage private val language: String,
) : MovieRepository {

    override suspend fun popularMovies(page: Int): Result<MoviePage> =
        withContext(dispatchers.io) {
            safeApiCall { api.popularMovies(page = page, language = language).toMoviePage() }
        }

    override suspend fun searchMovies(query: String, page: Int): Result<MoviePage> =
        withContext(dispatchers.io) {
            safeApiCall {
                api.searchMovies(query = query, page = page, language = language).toMoviePage()
            }
        }

    override suspend fun movieDetails(movieId: Int): Result<MovieDetails> =
        withContext(dispatchers.io) {
            safeApiCall { api.movieDetails(movieId = movieId, language = language).toDomain() }
        }

    private suspend fun MoviePageDto.toMoviePage() = MoviePage(
        page = page,
        movies = results.map { dto -> dto.toDomain(genreCache.namesFor(dto.genreIds, language)) },
        totalPages = totalPages,
    )
}
