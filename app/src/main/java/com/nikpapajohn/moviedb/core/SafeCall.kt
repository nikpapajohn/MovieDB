package com.nikpapajohn.moviedb.core

import java.io.IOException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

/**
 * Runs a network call and turns anything thrown into an [AppError].
 * Kotlin's own Result is used, so callers can fold without a custom wrapper type.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (e: IOException) {
    Result.failure(AppErrorException(AppError.Network))
} catch (e: HttpException) {
    val error = if (e.code() == 401 || e.code() == 403) AppError.Unauthorized else AppError.Http(e.code())
    Result.failure(AppErrorException(error))
} catch (e: SerializationException) {
    Result.failure(AppErrorException(AppError.Unknown(e)))
} catch (e: Exception) {
    Result.failure(AppErrorException(AppError.Unknown(e)))
}

/** The [AppError] behind a failure, falling back to [AppError.Unknown]. */
fun Throwable.toAppError(): AppError = when (this) {
    is AppErrorException -> error
    is IOException -> AppError.Network
    is HttpException -> if (code() == 401 || code() == 403) AppError.Unauthorized else AppError.Http(code())
    else -> AppError.Unknown(this)
}
