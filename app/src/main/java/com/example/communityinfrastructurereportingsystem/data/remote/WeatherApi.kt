package com.example.communityinfrastructurereportingsystem.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ── The JSON we receive, as Kotlin classes (Moshi fills them in) ──
// @JsonClass(generateAdapter = true): Moshi generates the converter at build time
@JsonClass(generateAdapter = true)
data class WeatherResponse(
    val current: CurrentWeather,
    val daily: DailyWeather
)

@JsonClass(generateAdapter = true)
data class CurrentWeather(
    // @Json links a JSON name to our Kotlin name
    @Json(name = "temperature_2m") val temperature: Double,
    val precipitation: Double
)

@JsonClass(generateAdapter = true)
data class DailyWeather(
    // These are lists with one value per day (we ask for 1 day). Null = no data
    @Json(name = "precipitation_sum") val precipitationSum: List<Double?>,
    @Json(name = "precipitation_probability_max") val rainChance: List<Int?>
)

// ── The request: Retrofit turns this interface into a real HTTP call ──
interface WeatherService {
    @GET("v1/forecast?current=temperature_2m,precipitation&daily=precipitation_sum,precipitation_probability_max&timezone=Africa/Nairobi&forecast_days=1")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): WeatherResponse
}

// ── One shared Retrofit object for the whole app ──
object WeatherApi {
    private const val BASE_URL = "https://api.open-meteo.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // by lazy: built the first time it is used
    val service: WeatherService by lazy {
        Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .baseUrl(BASE_URL)
            .build()
            .create(WeatherService::class.java)
    }
}