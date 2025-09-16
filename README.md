<pre>
Weather (Android) — Offline-First MVVM
 -> A modern Android weather app built with Kotlin, Jetpack Compose, and an offline-first architecture. 
 -> The app shows instant weather from a local snapshot, then quietly refreshes data in the background.

Features
 -> Offline-first snapshots: Weather responses are cached in Room per locationId + unit. City label is saved too for instant display.
 -> Instant launch: UI seeds from the local snapshot—no blocking spinners over existing content.
 -> Background refresh: After seeding from cache, the app refreshes in the background. 
    Favorites can be refreshed via WorkManager after the main screen updates.
 -> Location + Search: Search a city, navigate to main, and the app stores a snapshot for offline use.
    Last location is persisted in DataStore for the next launch.
 -> Favorites: List of previously viewed cities. Open shows cached data first; background refresh updates when needed.
 -> Permission gating: Lightweight gate for location permission & device settings (Resolvable API flow).

Tech Stack
  -> UI: Jetpack Compose
  -> DI: Hilt
  -> Async: Coroutines + Flow
  -> Storage: Room (Snapshots, Favorites), DataStore (preferences + last location)
  -> Network: Retrofit/OkHttp (OpenWeather One Call + Geocoding)
  -> BG work: refresh favorites
  -> Location: Google Play Services Location


Project Structure (condensed)

com.example.weather
├─ app/                         # Application & Activity
├─ core/                        # Reusable utils (async, design, util)
│  ├─ async/                    # DispatcherProvider, CoroutineExt
│  ├─ design/{theme,components} # AppTopBar, theme
│  └─ util/                     # Result, Extensions
├─ data/
│  ├─ local/
│  │  ├─ db/{WeatherDatabase, dao/, entity/}
│  │  └─ datastore/{UserPreferencesDataSource, LastLocationDataSource}
│  ├─ remote/{WeatherApi, dto/, WeatherRemoteDataSource}
│  ├─ mapper/{Dto↔Domain, Entity↔Domain}
│  └─ repository/*Impl          # WeatherRepositoryImpl, etc.
├─ domain/
│  ├─ model/                    # Weather, City, Favorite, GeoPoint, Units
│  ├─ repository/               # Interfaces only
│  └─ usecase/                  # GetWeather, SearchCity, ToggleFavorite, etc.
├─ ui/
│  ├─ navigation/{Destinations, NavGraph}
│  ├─ visuals/{WeatherVisualSpec, WeatherVisualMapper}
│  └─ feature/
│     ├─ main/{MainScreen, MainViewModel, components/}
│     ├─ search/{SearchScreen, SearchViewModel, components/}
│     ├─ favorite/{FavoriteScreen, FavoriteViewModel}
│     ├─ settings/{SettingsScreen, SettingsViewModel}
│     └─ about/{AboutScreen}
└─ di/{AppModule}



</pre>





https://github.com/user-attachments/assets/47f5a8a1-f5ca-4b12-b74e-cbaaeef053dd



