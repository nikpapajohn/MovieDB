package com.nikpapajohn.moviedb.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * TMDB v4 auth: a bearer token in a header. Deliberately not the ?api_key= form,
 * which ends up in server logs, proxies and crash reports.
 */
class AuthInterceptor(private val readAccessToken: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $readAccessToken")
            .addHeader("Accept", "application/json")
            .build()
        return chain.proceed(request)
    }
}
