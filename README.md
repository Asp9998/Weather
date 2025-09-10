# Weather
Developed a Weather Forecast Android app using Jetpack Compose, MVVM, and Hilt. It fetches real-time weather by city or coordinates, supports favorites, settings, and uses Room + Flow for local data. Built with clean architecture principles.

weather-forecast/
├─ app/
│  ├─ src/
│  │  ├─ main/
│  │  │  ├─ AndroidManifest.xml
│  │  │  ├─ java/com/example/weather/
│  │  │  │  ├─ WeatherApplication.kt          # @HiltAndroidApp
│  │  │  │  ├─ MainActivity.kt                # NavHost + app scaffold
│  │  │  │  ├─ navigation/                    # Routes, NavGraph, destinations
│  │  │  │  ├─ screens/                       # Feature screens (MVVM)
│  │  │  │  │  ├─ splash/
│  │  │  │  │  ├─ search/
│  │  │  │  │  ├─ main/
│  │  │  │  │  ├─ favorite/
│  │  │  │  │  ├─ settings/
│  │  │  │  │  └─ about/
│  │  │  │  ├─ components/                    # Reusable Compose UI pieces
│  │  │  │  ├─ widgets/                       # Small UI widgets (chips, loaders)
│  │  │  │  ├─ ui/theme/                      # Colors.kt, Type.kt, Theme.kt
│  │  │  │  ├─ data/                          # Data layer (local/remote)
│  │  │  │  │  ├─ local/                      # Room entities/DAO/DB
│  │  │  │  │  │  ├─ WeatherDao.kt
│  │  │  │  │  │  └─ WeatherDatabase.kt
│  │  │  │  │  └─ remote/                     # Retrofit API + DTOs
│  │  │  │  │     ├─ WeatherApi.kt
│  │  │  │  │     └─ dto/
│  │  │  │  ├─ repository/                    # WeatherRepository, WeatherDbRepository
│  │  │  │  ├─ di/                            # Hilt modules (AppModule.kt, etc.)
│  │  │  │  ├─ model/                         # Domain models + mappers
│  │  │  │  ├─ utils/                         # DataOrException, extensions, formatters
│  │  │  │  └─ buildconfig/                   # BuildConfig helpers (API keys)
│  │  │  ├─ res/                              # drawables, mipmap, values, etc.
│  │  ├─ androidTest/java/com/example/weather/ # Instrumentation tests
│  │  └─ test/java/com/example/weather/        # Unit tests (ViewModels, repos)
├─ build.gradle.kts
├─ settings.gradle.kts
├─ gradle.properties
├─ proguard-rules.pro
├─ gradle/
├─ gradlew
├─ gradlew.bat
└─ README.md




https://github.com/user-attachments/assets/620a4e31-c128-4cb5-b2a7-70a4d2a23682

