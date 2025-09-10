# Weather
Developed a Weather Forecast Android app using Jetpack Compose, MVVM, and Hilt. It fetches real-time weather by city or coordinates, supports favorites, settings, and uses Room + Flow for local data. Built with clean architecture principles.



<pre>
weather-forecast/
|-- app/
|   |-- src/
|   |   |-- main/
|   |   |   |-- AndroidManifest.xml
|   |   |   |-- java/com/example/weather/
|   |   |   |   |-- WeatherApplication.kt
|   |   |   |   |-- MainActivity.kt
|   |   |   |   |-- navigation/
|   |   |   |   |-- screens/
|   |   |   |   |   |-- splash/
|   |   |   |   |   |-- search/
|   |   |   |   |   |-- main/
|   |   |   |   |   |-- favorite/
|   |   |   |   |   |-- settings/
|   |   |   |   |   |-- about/
|   |   |   |   |-- components/
|   |   |   |   |   |-- MainScreenComponents.kt
|   |   |   |   |   |-- SearchScreenComponents.kt
|   |   |   |   |-- widgets/
|   |   |   |   |-- ui/theme/
|   |   |   |   |-- data/
|   |   |   |   |   |-- DataOrException.kt
|   |   |   |   |   |-- WeatherDao.kt
|   |   |   |   |   |-- WeatherDatabase.kt
|   |   |   |   |-- di/
|   |   |   |   |   |-- AppModule.kt
|   |   |   |   |-- model/
|   |   |   |   |-- network/
|   |   |   |   |-- repository/
|   |   |   |   |   |-- WeatherDbRepository.kt
|   |   |   |   |   |-- WeatherRepository.kt
|   |   |   |   |-- utils/
|   |   |   |-- res/
|   |   |-- androidTest/java/com/example/weather/
|   |   |-- test/java/com/example/weather/
|   |-- build.gradle.kts
|   |-- proguard-rules.pro
|-- gradle/
|-- gradle.properties
|-- settings.gradle.kts
|-- build.gradle.kts
|-- gradlew
|-- gradlew.bat
|-- README.md
</pre>




https://github.com/user-attachments/assets/620a4e31-c128-4cb5-b2a7-70a4d2a23682

