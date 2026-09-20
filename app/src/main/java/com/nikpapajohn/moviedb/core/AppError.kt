package com.nikpapajohn.moviedb.core

/**
 * Every failure the UI can meet, as a type. Exceptions stop at the data layer so that
 * no Retrofit or IO class ever reaches a ViewModel.
 */
sealed interface AppError {
    /** No connectivity, DNS failure, timeout. */
    data object Network : AppError

    /** TMDB refused the token (401/403). Almost always a bad or missing read access token. */
    data object Unauthorized : AppError

    /** Any other non-2xx response. */
    data class Http(val code: Int) : AppError

    /** Malformed payload, or anything we did not anticipate. */
    data class Unknown(val cause: Throwable? = null) : AppError
}

/** [cause] is kept so the original failure (timeout vs DNS vs TLS) survives for logging. */
class AppErrorException(val error: AppError, cause: Throwable? = null) :
    Exception(error.toString(), cause)
