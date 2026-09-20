package com.nikpapajohn.moviedb.core

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

/**
 * Runs a network call and turns anything thrown into an [AppError].
 * Kotlin's own Result is used, so callers can fold without a custom wrapper type.
 *
 * Cancellation is rethrown rather than mapped: a CancellationException is an ordinary
 * Exception, so without the first branch below, cancelling a scope (leaving the screen) or
 * a job (the next keystroke in search) would come back as a normal failure and the caller
 * would write an error into the state of a screen the user has already left.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: IOException) {
    Result.failure(AppErrorException(AppError.Network, e))
} catch (e: HttpException) {
    val error = if (e.code() == 401 || e.code() == 403) AppError.Unauthorized else AppError.Http(e.code())
    Result.failure(AppErrorException(error, e))
} catch (e: SerializationException) {
    Result.failure(AppErrorException(AppError.Unknown(e)))
} catch (e: Exception) {
    Result.failure(AppErrorException(AppError.Unknown(e)))
}

/**
 * The local counterpart of [safeApiCall], for work that does not go over the network:
 * DataStore writes and the Keystore crypto behind them. Both can fail (a full disk, a key
 * invalidated between read and write), and a failure inside `viewModelScope.launch` has no
 * handler above it — it reaches the default uncaught handler and takes the app down. This
 * turns it into a value the ViewModel can show, while still letting cancellation through.
 */
suspend fun <T> safeCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(AppErrorException(e.toAppError(), e))
}

/** The [AppError] behind a failure, falling back to [AppError.Unknown]. */
fun Throwable.toAppError(): AppError = when (this) {
    is AppErrorException -> error
    is IOException -> AppError.Network
    is HttpException -> if (code() == 401 || code() == 403) AppError.Unauthorized else AppError.Http(code())
    else -> AppError.Unknown(this)
}
