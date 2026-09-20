package com.nikpapajohn.moviedb.core

import com.nikpapajohn.moviedb.BuildConfig

/**
 * TMDB returns relative poster paths ("/abc.jpg"); the size is a presentation decision,
 * so the URL is built in the UI layer rather than baked into the domain model.
 */
enum class PosterSize(val path: String) {
    LIST("w185"),
    DETAILS("w500")
}

fun posterUrl(posterPath: String?, size: PosterSize): String? =
    posterPath?.takeIf { it.isNotBlank() }?.let { BuildConfig.TMDB_IMAGE_BASE_URL + size.path + it }
