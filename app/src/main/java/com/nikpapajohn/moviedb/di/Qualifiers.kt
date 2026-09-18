package com.nikpapajohn.moviedb.di

import javax.inject.Qualifier

/** TMDB "language" query parameter, derived once from the device locale. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TmdbLanguage
