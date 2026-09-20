package com.nikpapajohn.moviedb.data.remote

import com.nikpapajohn.moviedb.data.remote.dto.GenreListDto
import com.nikpapajohn.moviedb.data.remote.dto.MovieDetailsDto
import com.nikpapajohn.moviedb.data.remote.dto.MoviePageDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {

    @GET("movie/popular")
    suspend fun popularMovies(
        @Query("page") page: Int,
    ): MoviePageDto

    @GET("movie/{movie_id}")
    suspend fun movieDetails(
        @Path("movie_id") movieId: Int,
    ): MovieDetailsDto

    @GET("genre/movie/list")
    suspend fun movieGenres(): GenreListDto

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int,
    ): MoviePageDto
}
