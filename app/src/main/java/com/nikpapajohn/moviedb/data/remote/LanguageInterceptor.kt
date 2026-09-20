package com.nikpapajohn.moviedb.data.remote

import java.util.Locale
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Appends TMDB's "language" query parameter to every request, read fresh on each call
 * instead of once when the Hilt graph was built. A value cached in a @Singleton would miss
 * a locale change that does not restart the process — the Android 13+ per-app language
 * picker, for instance — so [currentLanguage] is asked again per request instead.
 *
 * [currentLanguage] defaults to the device locale but can be overridden, so tests pin a
 * language instead of depending on whatever locale the JVM running them happens to have.
 */
class LanguageInterceptor(
    private val currentLanguage: () -> String = { languageForLocale(Locale.getDefault()) },
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url.newBuilder()
            .setQueryParameter("language", currentLanguage())
            .build()
        return chain.proceed(original.newBuilder().url(url).build())
    }
}

internal fun languageForLocale(locale: Locale): String =
    if (locale.language == "el") "el-GR" else "en-US"
