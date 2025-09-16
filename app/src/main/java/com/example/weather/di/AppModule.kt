package com.example.weather.di

import android.content.Context
import androidx.room.Room
import com.example.weather.core.async.DefaultDispatchProvider
import com.example.weather.core.async.DispatcherProvider
import com.example.weather.data.local.db.dao.SnapshotDao
import com.example.weather.data.local.db.dao.WeatherDao
import com.example.weather.data.local.db.WeatherDatabase
import com.example.weather.data.remote.WeatherService
import com.example.weather.data.local.dataStore.LastLocationDataStore
import com.example.weather.data.remote.WeatherApi
import com.example.weather.domain.repository.LocationRepo
import com.example.weather.domain.repository.SnapshotRepository
import com.example.weather.domain.repository.WeatherDbRepository
import com.example.weather.core.util.Constants
import com.google.android.gms.location.LocationServices
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

//
//@Module
//@InstallIn(SingletonComponent::class)
//class AppModule {
//
//    // So, as we are using the same BASE_URL for One call Api and GeoCoding Api,
//    //      - there is no need to create two instances of same BASE_URL
//    //      - it can be done using single instance
////    @Provides
////    @Singleton
////    fun provideOpenWeatherApi(): WeatherApi{
////        return Retrofit.Builder()
////            .baseUrl(Constants.BASE_URL)
////            .addConverterFactory(GsonConverterFactory.create())
////            .build()
////            .create(WeatherApi::class.java)
////    }
//    @Provides @Singleton
//    fun provideRetrofit(
//        moshi: Moshi,
//        client: OkHttpClient
//    ): Retrofit =
//        Retrofit.Builder()
//            .baseUrl(Constants.BASE_URL)                // must end with '/'
//            .addConverterFactory(
//                retrofit2.converter.moshi.MoshiConverterFactory.create(moshi)
//            )
//            .client(client)
//            .build()
//
//    @Provides
//    @Singleton
//    fun provideWeatherDatabase(@ApplicationContext context: Context): WeatherDatabase{
//        return Room.databaseBuilder(
//            context,
//            WeatherDatabase::class.java,
//            "weather_database")
//            .fallbackToDestructiveMigration()
//            .build()
//    }
//
//    // Provide Weather Dao
//    @Provides
//    @Singleton
//    fun provideWeatherDao(weatherDatabase: WeatherDatabase): WeatherDao
//    = weatherDatabase.weatherDao()
//
//    // Provide Snapshot Dao
//    @Provides
//    @Singleton
//    fun providesSnapshotDao(weatherDatabase: WeatherDatabase): SnapshotDao =
//        weatherDatabase.snapshotDao()
//
//
//    @Provides
//    @Singleton
//    fun provideWeatherDbRepository(weatherDao: WeatherDao): WeatherDbRepository {
//        return WeatherDbRepository(weatherDao) // Or however you instantiate it
//    }
//
//    @Provides
//    @Singleton
//    fun provideLocationRepo(
//        @ApplicationContext context: Context,
//        dp: DispatcherProvider = DefaultDispatchProvider()
//    ): LocationRepo = LocationRepo(context, dp = dp)
//
//    // If you also want to inject these:
//    @Provides
//    @Singleton
//    fun provideFusedLocationProviderClient(
//        @ApplicationContext context: Context
//    ) = LocationServices.getFusedLocationProviderClient(context)
//
//    @Provides
//    @Singleton
//    fun provideSettingsClient(
//        @ApplicationContext context: Context
//    ) = LocationServices.getSettingsClient(context)
//
//    @Provides
//    @Singleton
//    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatchProvider()
//
//    @Provides @Singleton
//    fun provideMoshi(): Moshi =
//        Moshi.Builder()
//            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
//            .build()
//
//    @Provides
//    @Singleton
//    fun provideWeatherService(retrofit: Retrofit): WeatherService =
//        retrofit.create(WeatherService::class.java)
//
//    @Provides
//    @Singleton
//    fun providesSnapshotRepository(
//        dao: SnapshotDao,
//        weatherServices: WeatherService,
//        dp: DispatcherProvider = DefaultDispatchProvider(),
//        moshi: Moshi
//    ): SnapshotRepository = SnapshotRepository(
//        dao, weatherServices, dp,
//        moshi = moshi
//    )
//
//
//    @Provides @Singleton
//    fun provideOkHttpClient(): OkHttpClient =
//        OkHttpClient.Builder()
//            // .addInterceptor(logging)
//            // .addInterceptor(apiKeyInterceptor) // if your API needs it
//            .build()
//
//
//}


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- Network stack ---
    @Provides @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder()
            .add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
            .build()

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
//             .addInterceptor(logging)
//             .addInterceptor(apiKeyInterceptor)
            .build()

    @Provides @Singleton
    fun provideRetrofit(
        moshi: Moshi,
        client: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL) // MUST end with '/'
            .addConverterFactory(
                retrofit2.converter.moshi.MoshiConverterFactory.create(moshi)
            )
            .client(client)
            .build()

    // Create both services from the same Retrofit
    @Provides @Singleton
    fun provideWeatherService(retrofit: Retrofit): WeatherService =
        retrofit.create(WeatherService::class.java)

    @Provides @Singleton
    fun provideOpenWeatherApi(retrofit: Retrofit): WeatherApi =
        retrofit.create(WeatherApi::class.java)

    // --- Room ---
    @Provides @Singleton
    fun provideWeatherDatabase(@ApplicationContext context: Context): WeatherDatabase =
        Room.databaseBuilder(context, WeatherDatabase::class.java, "weather_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideWeatherDao(db: WeatherDatabase): WeatherDao = db.weatherDao()

    @Provides @Singleton
    fun provideSnapshotDao(db: WeatherDatabase): SnapshotDao = db.snapshotDao()

    // --- Dispatchers & location ---
    @Provides @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatchProvider()

    @Provides @Singleton
    fun provideLocationRepo(
        @ApplicationContext context: Context,
        dp: DispatcherProvider
    ): LocationRepo = LocationRepo(context, dp = dp)

    // Optional, if you inject these directly elsewhere
    @Provides @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context) =
        LocationServices.getFusedLocationProviderClient(context)

    @Provides @Singleton
    fun provideSettingsClient(@ApplicationContext context: Context) =
        LocationServices.getSettingsClient(context)

    // --- Other repos you still use ---
    @Provides @Singleton
    fun provideWeatherDbRepository(weatherDao: WeatherDao): WeatherDbRepository =
        WeatherDbRepository(weatherDao)

    // Prefer constructor injection in SnapshotRepository, but if you keep a provider:
    @Provides @Singleton
    fun providesSnapshotRepository(
        dao: SnapshotDao,
        weatherServices: WeatherService,
        dp: DispatcherProvider,
        moshi: Moshi,
        @ApplicationContext context: android.content.Context
    ): SnapshotRepository = SnapshotRepository(
        dao = dao,
        weatherService = weatherServices,
        dp = dp,
        context = context,
        moshi = moshi
    )


    @Provides
    @Singleton
    fun provideLastLocationStore(@ApplicationContext ctx: Context): LastLocationDataStore =
        LastLocationDataStore(ctx)
}

