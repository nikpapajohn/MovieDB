package com.nikpapajohn.moviedb.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikpapajohn.moviedb.R
import com.nikpapajohn.moviedb.core.UiText
import com.nikpapajohn.moviedb.core.safeCall
import com.nikpapajohn.moviedb.core.toAppError
import com.nikpapajohn.moviedb.domain.usecase.ClearFavoritesUseCase
import com.nikpapajohn.moviedb.domain.usecase.ObserveFavoriteIdsUseCase
import com.nikpapajohn.moviedb.ui.profile.ProfileContract.Change
import com.nikpapajohn.moviedb.ui.profile.ProfileContract.Effect
import com.nikpapajohn.moviedb.ui.profile.ProfileContract.Intent
import com.nikpapajohn.moviedb.ui.profile.ProfileContract.State
import com.nikpapajohn.moviedb.ui.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeFavoriteIds: ObserveFavoriteIdsUseCase,
    private val clearFavorites: ClearFavoritesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val _effects = Channel<Effect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            observeFavoriteIds().collect { ids -> update(Change.CountUpdated(ids.size)) }
        }
    }

    fun onIntent(intent: Intent) {
        when (intent) {
            // "All favorites cleared" must only be said once the write actually succeeded;
            // an unguarded failure here would reach the default handler and crash the app.
            Intent.ClearFavoritesClicked -> viewModelScope.launch {
                val message = safeCall { clearFavorites() }.fold(
                    onSuccess = { UiText.res(R.string.message_favorites_cleared) },
                    onFailure = { it.toAppError().toUiText() },
                )
                _effects.send(Effect.ShowMessage(message))
            }
        }
    }

    private fun update(change: Change) {
        _state.update { ProfileReducer.reduce(it, change) }
    }
}
