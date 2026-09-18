package com.nikpapajohn.moviedb.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nikpapajohn.moviedb.R
import com.nikpapajohn.moviedb.core.UiText
import com.nikpapajohn.moviedb.domain.usecase.ClearFavoritesUseCase
import com.nikpapajohn.moviedb.domain.usecase.ObserveFavoriteIdsUseCase
import com.nikpapajohn.moviedb.ui.profile.ProfileContract.Change
import com.nikpapajohn.moviedb.ui.profile.ProfileContract.Effect
import com.nikpapajohn.moviedb.ui.profile.ProfileContract.Intent
import com.nikpapajohn.moviedb.ui.profile.ProfileContract.State
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
            Intent.ClearFavoritesClicked -> viewModelScope.launch {
                clearFavorites()
                _effects.send(Effect.ShowMessage(UiText.res(R.string.message_favorites_cleared)))
            }
        }
    }

    private fun update(change: Change) {
        _state.update { ProfileContract.reduce(it, change) }
    }
}
