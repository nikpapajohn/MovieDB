package com.nikpapajohn.moviedb.di

import com.nikpapajohn.moviedb.core.DefaultDispatcherProvider
import com.nikpapajohn.moviedb.core.DispatcherProvider
import com.nikpapajohn.moviedb.data.repository.FavoritesRepositoryImpl
import com.nikpapajohn.moviedb.data.repository.MovieRepositoryImpl
import com.nikpapajohn.moviedb.domain.repository.FavoritesRepository
import com.nikpapajohn.moviedb.domain.repository.MovieRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMovieRepository(impl: MovieRepositoryImpl): MovieRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    companion object {
        @Provides
        @Singleton
        fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()
    }
}
