package com.nikpapajohn.moviedb.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.nikpapajohn.moviedb.data.local.crypto.CryptoManager
import com.nikpapajohn.moviedb.data.local.crypto.KeystoreCryptoManager
import com.nikpapajohn.moviedb.data.local.favorites.EncryptedFavoritesSerializer
import com.nikpapajohn.moviedb.data.local.favorites.FavoritesData
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    // @Binds, not @Provides: KeystoreCryptoManager already has an @Inject constructor, so
    // binding the interface to it generates less code than constructing it by hand.
    @Binds
    @Singleton
    abstract fun bindCryptoManager(impl: KeystoreCryptoManager): CryptoManager

    companion object {
        @Provides
        @Singleton
        fun provideFavoritesDataStore(
            @ApplicationContext context: Context,
            crypto: CryptoManager
        ): DataStore<FavoritesData> = DataStoreFactory.create(
            serializer = EncryptedFavoritesSerializer(crypto),
            // A key that can no longer decrypt the file loses the favorites, but never crashes the app.
            corruptionHandler = ReplaceFileCorruptionHandler { FavoritesData() },
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { context.dataStoreFile(FAVORITES_FILE) }
        )

        private const val FAVORITES_FILE = "favorites.enc"
    }
}
