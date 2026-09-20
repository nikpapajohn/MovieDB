package com.nikpapajohn.moviedb.domain.usecase

import com.nikpapajohn.moviedb.domain.model.Movie
import com.nikpapajohn.moviedb.domain.model.MovieDetails
import com.nikpapajohn.moviedb.domain.model.MoviePage
import com.nikpapajohn.moviedb.domain.repository.FavoritesRepository
import com.nikpapajohn.moviedb.domain.repository.MovieRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetPopularMoviesUseCase @Inject constructor(private val repository: MovieRepository) {
    suspend operator fun invoke(page: Int): Result<MoviePage> = repository.popularMovies(page)
}

class SearchMoviesUseCase @Inject constructor(private val repository: MovieRepository) {
    suspend operator fun invoke(query: String, page: Int): Result<MoviePage> =
        repository.searchMovies(query, page)
}

class GetMovieDetailsUseCase @Inject constructor(private val repository: MovieRepository) {
    suspend operator fun invoke(movieId: Int): Result<MovieDetails> =
        repository.movieDetails(movieId)
}

class ObserveFavoriteIdsUseCase @Inject constructor(private val repository: FavoritesRepository) {
    operator fun invoke(): Flow<Set<Int>> = repository.favoriteIds()
}

class ObserveFavoritesUseCase @Inject constructor(private val repository: FavoritesRepository) {
    operator fun invoke(): Flow<List<Movie>> = repository.favorites()
}

class ObserveIsFavoriteUseCase @Inject constructor(private val repository: FavoritesRepository) {
    operator fun invoke(movieId: Int): Flow<Boolean> = repository.isFavorite(movieId)
}

class ToggleFavoriteUseCase @Inject constructor(private val repository: FavoritesRepository) {
    /** Returns true when the movie is a favorite after the toggle. */
    suspend operator fun invoke(movie: Movie): Boolean = repository.toggle(movie)
}

class ClearFavoritesUseCase @Inject constructor(private val repository: FavoritesRepository) {
    suspend operator fun invoke() = repository.clear()
}

class RefreshFavoritesUseCase @Inject constructor(private val repository: FavoritesRepository) {
    suspend operator fun invoke() = repository.refresh()
}
