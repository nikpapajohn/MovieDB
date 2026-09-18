# MovieDB — Android Technical Assessment

A TMDB client in Kotlin and Jetpack Compose, built with **MVI**: a paged list of popular
movies, a details screen, and favorites stored **encrypted with a key held in the Android
Keystore**.

## Setup

1. Get a TMDB **API Read Access Token** (TMDB → Settings → API).
2. Add it to `local.properties` in the project root (the file is git-ignored):

   ```properties
   TMDB_READ_ACCESS_TOKEN=eyJhbGciOiJIUzI1NiJ9....
   ```

   `CI` can pass the same value through the `TMDB_READ_ACCESS_TOKEN` environment variable.
3. Open in Android Studio (JDK 17) and run. `minSdk 26`, `compileSdk 35`.

> The Gradle wrapper binary is not committed here. Android Studio generates it on the first
> sync; from a terminal, `gradle wrapper --gradle-version 8.9` once, then `./gradlew` as usual.

Without a token every request comes back 401 and the app shows a message that says exactly
that, instead of an empty list.

## What it does

| Screen | Contents |
| --- | --- |
| Home | Popular movies, paged. Poster, title, rating, genres, and a bookmark that favorites the movie without opening it. Search is in the top bar. |
| Details | Poster, title, tagline, rating with vote count, genre chips, release year, runtime, overview, and the add/remove favorites button. |
| Favorites | The encrypted store, rendered with the same card. Works with no network. |
| Profile | How favorites are stored, a way to clear them, version and TMDB attribution. |

## Architecture

MVI on top of three layers in one module. Dependencies point inwards: `domain` knows
nothing about Retrofit, Compose or Android.

```
ui/       Compose screens + ViewModels, one Contract per feature
domain/   models, repository interfaces, use cases
data/     TMDB (Retrofit), encrypted local storage, mappers
core/     AppError, safeApiCall, UiText, DispatcherProvider, image URL builder
di/       Hilt modules
```

Each feature owns a `Contract` with four types:

| Type | Meaning |
| --- | --- |
| `State` | Everything the screen shows. Immutable, one object. |
| `Intent` | Everything the user can do. The ViewModel has a single entry point, `onIntent`. |
| `Effect` | What happens once and is not state: navigation, a snackbar. Delivered over a `Channel`. |
| `Change` | The result of work, folded into the state by a pure reducer. Never leaves the ViewModel. |

There is deliberately **no generic `BaseMviViewModel`**. With four screens, a shared
abstraction would cost more in indirection than it saves in duplication.

Points worth calling out:

- **The reducer is pure.** `PopularReducer.reduce(state, change)` has no coroutines and no
  Android dependencies, so every transition — first page, append, dedup, append failure,
  favorites arriving from elsewhere — is a plain unit test.
- **Errors are types, not exceptions.** `safeApiCall` turns everything into
  `AppError.{Network, Unauthorized, Http, Unknown}`, and `UiText` carries the message to
  the UI without giving the ViewModel a `Context`.
- **The favorite flag is not part of the TMDB model.** Favorite ids arrive as their own
  flow and the reducer re-marks the rows already on screen, so favoriting never re-fetches
  and the list is already correct when you come back from the details screen.
- **Genres in the list.** `movie/popular` returns genre ids only, so `GenreCache` fetches
  `genre/movie/list` once per process. If that call fails the list still renders, without
  genre labels.
- **Dispatchers are injected** (`DispatcherProvider`) so tests are deterministic.

### Why pagination is hand-written, not Paging 3

Paging 3 is the usual answer, and it is a good one — but `PagingData` is a stream with its
own lifecycle, not a value. It cannot live inside an immutable state that goes through a
reducer, so adopting it would mean MVI everywhere except the main screen: part of the
state in `State`, part in a separate flow, and `LazyPagingItems` inside the composable.

Pagination here is therefore explicit: `page`, `endReached`, `isLoadingMore` and the loaded
items are ordinary fields, `Intent.LoadNextPage` fires when the last row appears, and the
reducer merges pages and drops the duplicates TMDB returns when its ranking shifts between
requests. The cost is roughly forty lines; the gain is one coherent state per screen and
tests that need no extra artifact.

If the list became database-backed, needed offline caching or grew to thousands of rows,
`RemoteMediator` and Paging 3 would earn their place and the trade-off would flip.

## Favorites security

The requirement says "Keystore". The Keystore holds keys, not lists, so:

- **Key:** AES-256, generated in the `AndroidKeyStore` under the alias
  `moviedb_favorites_key`, `BLOCK_MODE_GCM`, no padding,
  `setRandomizedEncryptionRequired(true)` so the system issues a fresh IV per encryption.
  The key never leaves the keystore; the app only passes bytes in and out.
- **Payload:** `[1 byte version][12 byte IV][ciphertext || 16 byte GCM tag]`. The version
  byte leaves room to change the format later without breaking existing installs. GCM
  authenticates, so a modified file fails to decrypt rather than returning garbage.
- **Storage:** `DataStore<FavoritesData>` with a custom `Serializer` that encrypts in
  `writeTo` and decrypts in `readFrom`. DataStore handles atomic writes and coordination;
  the serializer only handles crypto. The plaintext is JSON holding a snapshot of each
  favorite (id, title, poster path, rating), so the favorites screen works offline.
- **Failure modes:** a key invalidated by a device restore or a lock-screen change throws
  `KeyPermanentlyInvalidatedException`; the key is recreated and the store falls back to
  empty through `ReplaceFileCorruptionHandler`. The user loses favorites, the app does not
  crash-loop.
- **`allowBackup=false`** plus explicit data-extraction rules: a cloud copy of the
  encrypted file without the key would be useless anyway, so the intent is documented.

Not used on purpose: `EncryptedSharedPreferences`. `androidx.security:security-crypto` is
deprecated, and it would hide the part of this exercise that is actually being assessed.

**Threat model, honestly.** This protects data at rest against another app, `adb`/backup
extraction and physical access to the device. It does not protect against a rooted device
where an attacker runs code inside this process.

**The API token.** `local.properties` → `BuildConfig`. That keeps it out of version
control, and the logging interceptor redacts the `Authorization` header, but
`BuildConfig` is not a security boundary — the string is recoverable from the APK. In
production the token belongs behind a backend proxy that the app talks to instead.

## Tests

```bash
./gradlew testDebugUnitTest          # reducer, ViewModels, mappers, repository (MockWebServer)
./gradlew connectedDebugAndroidTest  # Keystore crypto, encrypted DataStore, Compose UI
```

`PopularReducerTest` is the one to read first: it covers every state transition of the main
screen without a single mock or coroutine, which is the practical argument for MVI here.

The crypto tests are instrumented because the `AndroidKeyStore` only exists on a device or
emulator. The one worth reading is
`EncryptedFavoritesStoreTest.the_stored_file_never_holds_the_title_in_clear_text`: it reads
the DataStore file as bytes and asserts the movie title is not in it — one assertion that
proves the encryption is actually applied.

## Trade-offs

- **One module, layered packages.** Multi-module would look impressive but costs build
  complexity that this scope does not earn. The package boundaries are already where the
  module boundaries would go.
- **No Room.** With an encrypted store for favorites, a database would be a third source of
  truth with nothing to do. Offline caching of the popular list is the natural next step,
  not a missing piece.
- **Dynamic color off.** The assessment mockup specifies a palette; Material You would
  replace it with wallpaper colors on Android 12+.

## Next steps, given more time

- Room for an offline copy of the popular list; at that point Paging 3 with `RemoteMediator`
  becomes the right tool and the pagination above moves behind it.
- Saving the loaded page and scroll position in `SavedStateHandle` so process death restores
  more than the first page.
- GitHub Actions running `assembleDebug` and the unit tests, plus ktlint/detekt.

This product uses the TMDB API but is not endorsed or certified by TMDB.
