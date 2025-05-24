package com.example.weather.di

import android.content.Context
import androidx.room.Room
import com.example.weather.data.WeatherDao
import com.example.weather.data.WeatherDatabase
import com.example.weather.network.WeatherApi
import com.example.weather.repository.WeatherDbRepository
import com.example.weather.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    // So, as we are using the same BASE_URL for One call Api and GeoCoding Api,
    //      - there is no need to create two instances of same BASE_URL
    //      - it can be done using single instance
    @Provides
    @Singleton
    fun provideOpenWeatherApi(): WeatherApi{
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWeatherDatabase(@ApplicationContext context: Context): WeatherDatabase{
        return Room.databaseBuilder(
            context,
            WeatherDatabase::class.java,
            "weather_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideWeatherDao(weatherDatabase: WeatherDatabase): WeatherDao
    = weatherDatabase.weatherDao()

    @Provides
    @Singleton
    fun provideWeatherDbRepository(weatherDao: WeatherDao): WeatherDbRepository {
        return WeatherDbRepository(weatherDao) // Or however you instantiate it
    }
}
