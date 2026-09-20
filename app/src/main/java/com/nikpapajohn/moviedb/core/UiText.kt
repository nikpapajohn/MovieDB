package com.nikpapajohn.moviedb.core

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable

/**
 * A message a ViewModel can produce without holding a Context: either a literal string
 * (usually from a server) or a string resource with arguments, resolved in the UI layer.
 */
// @Immutable on the subtypes too, not just the interface: Res holds a List, which the
// compiler treats as unstable by default. Without this, every State carrying a UiText is
// unstable and the screens that take one can never skip recomposition.
@Immutable
sealed interface UiText {
    @Immutable
    data class Dynamic(val value: String) : UiText

    @Immutable
    data class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) : UiText

    companion object {
        fun res(@StringRes id: Int, vararg args: Any): Res = Res(id, args.toList())
    }
}
