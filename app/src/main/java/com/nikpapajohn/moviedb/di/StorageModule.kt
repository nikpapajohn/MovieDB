package com.nikpapajohn.moviedb.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.nikpapajohn.moviedb.data.local.crypto.CryptoManager
import com.nikpapajohn.moviedb.data.local.crypto.KeystoreCryptoManager
import com.nikpapajohn.moviedb.data.local.favorites.EncryptedFavoritesSerializer
import com.nikpapajohn.moviedb.data.local.favorites.FavoritesData
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Provides
    @Singleton
    fun provideCryptoManager(): CryptoManager = KeystoreCryptoManager()

    @Provides
    @Singleton
    fun provideFavoritesDataStore(
        @ApplicationContext context: Context,
        crypto: CryptoManager,
    ): DataStore<FavoritesData> = DataStoreFactory.create(
        serializer = EncryptedFavoritesSerializer(crypto),
        // A key that can no longer decrypt the file loses the favorites, but never crashes the app.
        corruptionHandler = ReplaceFileCorruptionHandler { FavoritesData() },
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.dataStoreFile(FAVORITES_FILE) },
    )

    private const val FAVORITES_FILE = "favorites.enc"
}
