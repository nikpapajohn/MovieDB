package com.nikpapajohn.moviedb.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.nikpapajohn.moviedb.BuildConfig
import com.nikpapajohn.moviedb.data.remote.AuthInterceptor
import com.nikpapajohn.moviedb.data.remote.LanguageInterceptor
import com.nikpapajohn.moviedb.data.remote.TmdbApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            // The token must never end up in logcat, a bug report or a crash log.
            redactHeader("Authorization")
        }
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(BuildConfig.TMDB_READ_ACCESS_TOKEN))
            // Added before the logger, so what gets logged is the request TMDB actually sees.
            .addInterceptor(LanguageInterceptor())
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            // connect/read bound each phase; callTimeout is the only one that bounds the
            // request as a whole, including redirects and retries.
            .callTimeout(45, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.TMDB_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideTmdbApi(retrofit: Retrofit): TmdbApi = retrofit.create(TmdbApi::class.java)
}
