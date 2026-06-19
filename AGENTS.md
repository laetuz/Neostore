# Neostore — neostore-fe v0.0.20 / Protocol v1.1

Legacy Android app store client targeting **Android 2.1+ (API 7)** — Eclair / Froyo / Gingerbread.

## Build & test

```sh
./gradlew assembleDebug              # build debug APK
./gradlew test                        # JUnit unit tests only
./gradlew connectedAndroidTest        # instrumentation tests (need device/emulator)
./gradlew :app:dependencies           # inspect dependency tree
```

## Key constraints

- **Java 7 only** (no Kotlin). Old-style Android: `android.app.Activity`, `AsyncTask`, `ListView` — **no** Jetpack, Fragments, ViewModels, RecyclerView, or AppCompat-v7 (libraries exist in `build.gradle` but must NOT be used in code).
- **`local.properties` is required but gitignored**. Must define `BASE_URL` and `FILE_BASE_URL` as **quoted strings** for `BuildConfig` injection:
  ```
  BASE_URL="http://dev.neotica.id/neostore"
  FILE_BASE_URL="http://storage.neotica.id"
  ```
- **Gradle 2.3.2** + Android SDK 21 / build-tools 25.0.0. Min SDK 7, target SDK 21.
- **No CI/CD** configured. No lint, formatter, or typecheck config.
- **No Retrofit/OkHttp**. Networking is `HttpURLConnection` inside `AsyncTask` (`ApiTask`, `DownloadTask`). Image loading via `com.nostra13.universalimageloader:universal-image-loader:1.9.5`.
- **ProGuard** rules in `app/proguard-rules.pro` — currently empty (minification disabled for release).

## API 7 gotchas (force-close prevention)

1. **String checking** — Never use `String.isEmpty()`. Always `TextUtils.isEmpty(string)` or `string.length() > 0`.
2. **`ListView.addFooterView()`** MUST be called **before** `listView.setAdapter()`.
3. **ViewHolder pattern** — All `ListAdapter.getView()` implementations must recycle via `convertView.setTag(viewHolder)` to prevent OOM on 512 MB devices. Already used in `AppAdapter`, `VersionAdapter`, `SectionAdapter`.
4. **No `SharedPreferences.Editor.apply()`** — Not available until API 9. Always use `.commit()` instead (available from API 1).
5. **No `?android:attr/selectableItemBackground`** — Not available until API 11. Use a plain color/drawable or remove for API < 11.
6. **No silent installs** — APK download must go through `DownloadTask` → `Environment.getExternalStorageDirectory()` → `Intent.ACTION_VIEW` with `application/vnd.android.package-archive` MIME type. Installs always require user confirmation.

## Architecture

```
app/src/main/java/id/neotica/neostore/
├── NeostoreApp.java              # Application class (init UIL)
├── model/                        # POJOs: AppModel, VersionModel, ItemModel, ItemDetail
├── network/
│   ├── ApiTask.java              # Generic AsyncTask for REST (GET/POST/PUT/DELETE)
│   ├── ApiCallback.java          # onSuccess / onError callback
│   └── DownloadTask.java         # APK download + auto-install via Intent
├── ui/
│   ├── home/
│   │   ├── MainActivity.java     # Launcher: sections + featured apps; auth gate
│   │   ├── LoginActivity.java    # Login screen (GET /auth/login with header creds)
│   │   ├── SettingsActivity.java # Profile screen with logout
│   │   ├── AppListActivity.java  # Paginated app list with search
│   │   ├── AppTopic.java         # Topic model (APPLICATION/GAME)
│   │   └── SectionAdapter.java   # Section list adapter
│   ├── detail/
│   │   └── AppDetailActivity.java# App detail + version list + download + icon
│   ├── AppAdapter.java           # App list adapter (ViewHolder pattern)
│   └── VersionAdapter.java       # Version list adapter (ViewHolder pattern)
└── utils/
    ├── Constants.java            # BASE_URL from BuildConfig
    ├── AuthManager.java          # SharedPreferences JWT storage + JWT decode
    └── CrashCatcher.java         # Uncaught exception handler → SharedPrefs
```

- **Entry point**: `NeostoreApp` → `MainActivity` (auth gate) → `AppListActivity` / `AppDetailActivity`.
- **API** uses raw JSON (`org.json.JSONObject`/`JSONArray`), no serialization library. Most endpoints (`/feed`, `/collections/featured`, `/apps/{package}`) do NOT need a Bearer token.
- **Auth**: Only `/auth/login` requires credentials — sent as **request headers** (not body/query). JWT stored in `AuthManager` (SharedPreferences). Username extracted from JWT payload via `getUsernameFromToken()`.
- **CrashCatcher** saves last crash stacktrace to SharedPreferences and shows an AlertDialog on next launch.
- **DownloadTask** saves APKs to `Environment.getExternalStorageDirectory()/NeoStore/` and auto-launches the package installer.

## Current milestone (v0.2 — Authentication)

Implemented:
- `LoginActivity.java` — sends GET to `http://dev.neotica.id/auth/login` with `username`/`password` as request headers
- `AuthManager` — `SharedPreferences`-backed JWT storage with `saveToken()`, `getToken()`, `isLoggedIn()`, `getUsernameFromToken()`, `getAuthHeaders()`
- `MainActivity` — login gate: shows login button when unauthenticated (launches `LoginActivity` without `finish()`); shows `"Welcome {username}!"` when logged in via JWT payload decode
- `ApiTask` — now supports custom headers via `Map<String, String>` constructor parameter

## Testing

- `app/src/test/` — boilerplate `ExampleUnitTest` (JUnit 4.12). Write new tests here.
- `app/src/androidTest/` — boilerplate `ExampleInstrumentedTest` (AndroidJUnitRunner). Write instrumentation tests here.
