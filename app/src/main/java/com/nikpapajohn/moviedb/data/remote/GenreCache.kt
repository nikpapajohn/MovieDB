package com.nikpapajohn.moviedb.data.remote

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * movie/popular returns genre ids, not names, but the list card shows names.
 * The genre list is small and near-static, so it is fetched once per process.
 * A failure here is not worth failing the list for: we fall back to no genre labels.
 */
@Singleton
class GenreCache @Inject constructor(
    private val api: TmdbApi,
) {
    private val mutex = Mutex()
    private var cache: Map<Int, String>? = null

    suspend fun namesFor(ids: List<Int>?): List<String> {
        if (ids.isNullOrEmpty()) return emptyList()
        val map = genres()
        return ids.mapNotNull { map[it] }
    }

    private suspend fun genres(): Map<Int, String> {
        cache?.let { return it }
        return mutex.withLock {
            cache ?: runCatching { api.movieGenres().genres.associate { it.id to it.name } }
                .getOrDefault(emptyMap())
                .also { if (it.isNotEmpty()) cache = it }
        }
    }
}
