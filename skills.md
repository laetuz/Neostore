# Neostore Development Skills

## 1. Adding a new API endpoint

Use `ApiTask` + `ApiCallback` — no Retrofit/OkHttp.

```java
String url = BuildConfig.BASE_URL + "/path";
new ApiTask(context, "GET", url, null, "Loading...", new ApiCallback() {
    @Override
    public void onSuccess(String response) {
        // Parse JSON from response string
    }
    @Override
    public void onError(String errorMessage) {
        // Handle error
    }
}).execute();
```

- GET/POST/PUT/DELETE supported — pass JSON string as 4th arg for POST/PUT.
- Custom headers: pass a `Map<String, String>` as the 7th constructor argument.
  ```java
  Map<String, String> headers = new HashMap<String, String>();
  headers.put("Authorization", "Bearer " + token);
  new ApiTask(context, "GET", url, null, "Loading...", callback, headers).execute();
  ```
- Error responses come back as `"HTTP_ERROR_{code}|{body}"` or `"NETWORK_ERROR_{msg}"`.
- `ApiCallback.onSuccess()` receives raw JSON string — parse with `org.json.JSONObject`/`JSONArray`.
- Most endpoints (`/feed`, `/collections/featured`, `/apps/{package}`) do NOT require auth.

## 2. Adding a new screen (Activity + ListView)

Follow the pattern in `AppListActivity`:
1. Extend `android.app.Activity` (not `AppCompatActivity`).
2. Inflate layout with `ListView`.
3. Create adapter extending `ArrayAdapter<T>` with a private static `ViewHolder` class.
4. **Must** call `listView.addFooterView()` **before** `listView.setAdapter()`.
5. `getView()` must recycle via `convertView.setTag(viewHolder)`.
6. Call `CrashCatcher.init()` in `onCreate()`, then `CrashCatcher.showCrashLogIfAny(this)`.

## 3. Authentication flow (current milestone)

Endpoint: `http://dev.neotica.id/auth/login` (GET, not POST).
- Pass `username` and `password` as **request headers** (not query params, not body).
- Use `ApiTask`'s 7th `Map<String, String>` parameter for custom headers:
  ```java
  Map<String, String> headers = new HashMap<String, String>();
  headers.put("username", username);
  headers.put("password", password);
  new ApiTask(context, "GET", url, null, "Logging in...", callback, headers).execute();
  ```
- On success, response is JSON: `{"token": "..", "refreshToken": "..", "expirationTime": ..}`.
- Extract `token` and store via `AuthManager`
  (`new AuthManager(context).saveToken(token)`).
- `AuthManager` also provides:
  - `getUsernameFromToken()` — decodes JWT payload, extracts `sub`/`username`/`name`/`id`.
  - `getAuthHeaders()` — returns `Map<String, String>` with `Authorization: Bearer {token}`.
  - `isLoggedIn()` / `clear()` / `saveRefreshToken()` / `saveExpirationTime()`.
- `MainActivity` acts as auth gate: shows login button when unauthenticated (does NOT `finish()`, so user returns after login); shows `"Welcome {username}!"` when logged in.

## 4. SharedPreferences-backed storage

Use `context.getSharedPreferences("NeostorePrefs", Context.MODE_PRIVATE)`.
- Already used by `CrashCatcher` (`"CrashLogs"`) for crash stacktraces.
- `AuthManager` uses `"NeostoreAuth"` pref file for JWT storage (`token`, `refreshToken`, `expirationTime`).
- **Always use `.commit()` instead of `.apply()`** — `apply()` requires API 9+; this app supports API 7.

## 5. APK download & install

Use `DownloadTask` (handles streaming, progress dialog, and auto-install):
```java
new DownloadTask(Activity.this, "app.apk").execute(downloadUrl);
```
- Saves to `Environment.getExternalStorageDirectory()/NeoStore/`.
- Fires `Intent.ACTION_VIEW` with `application/vnd.android.package-archive` MIME.
- User always confirms installation (no silent install possible on API 7).

## 6. Image loading

Universal Image Loader is initialized in `NeostoreApp.onCreate()`:
```java
ImageLoader.getInstance().displayImage(url, imageView);
```
- Use `BuildConfig.FILE_BASE_URL + "/buckets" + iconUrl` for app icons.
- Always cancel pending tasks on recycled views: `ImageLoader.getInstance().cancelDisplayTask(imageView)`.

## 7. App icon export

To generate correctly-sized mipmap icons from a single source PNG, use:
https://www.batchpngtools.com/android-icon-set-generator

Target densities: `mdpi` (48×48), `hdpi` (72×72), `xhdpi` (96×96), `xxhdpi` (144×144), `xxxhdpi` (192×192).
