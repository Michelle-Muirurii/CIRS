package com.example.communityinfrastructurereportingsystem.data.repository

import com.example.communityinfrastructurereportingsystem.data.model.NairobiConfig
import com.example.communityinfrastructurereportingsystem.data.remote.WeatherApi
import com.example.communityinfrastructurereportingsystem.data.remote.WeatherResponse

// The repository is the only class that knows where the weather comes from
class WeatherRepository {

    // suspend: runs in a coroutine, so the screen never freezes while waiting for the internet
    suspend fun getNairobiWeather(): WeatherResponse =
        WeatherApi.service.getForecast(NairobiConfig.CENTER_LAT, NairobiConfig.CENTER_LNG)
}