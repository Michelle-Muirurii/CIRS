package com.example.communityinfrastructurereportingsystem.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.communityinfrastructurereportingsystem.data.remote.WeatherResponse
import com.example.communityinfrastructurereportingsystem.data.repository.WeatherRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import kotlin.math.roundToInt

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _weatherText = MutableLiveData("Checking the weather in Nairobi...")
    val weatherText: LiveData<String> = _weatherText   // the screen just shows this text

    fun loadWeather() {
        viewModelScope.launch {
            _weatherText.value = "Checking the weather in Nairobi..."   // loading state
            try {
                _weatherText.value = buildMessage(repository.getNairobiWeather())
            } catch (e: IOException) {
                // No internet, or the server could not be reached
                _weatherText.value = "Weather unavailable (no internet). Tap to retry."
            } catch (e: HttpException) {
                // The server answered with an error code
                _weatherText.value = "Weather service error (${e.code()}). Tap to retry."
            } catch (e: Exception) {
                // The JSON was not what we expected
                _weatherText.value = "Could not read the weather data. Tap to retry."
            }
        }
    }

    // Turn the numbers into advice. Blocked drains + heavy rain = flooding
    private fun buildMessage(weather: WeatherResponse): String {
        val rainMm = weather.daily.precipitationSum.firstOrNull() ?: 0.0
        val chance = weather.daily.rainChance.firstOrNull() ?: 0
        val temperature = weather.current.temperature.roundToInt()

        val advice = when {
            rainMm >= 10 || chance >= 80 ->
                "Heavy rain expected. Flooding is possible: avoid flooded roads and report blocked drains."
            rainMm >= 1 || chance >= 50 ->
                "Rain is likely today. Watch out for flooded roads."
            else -> "No significant rain expected today."
        }
        return "Nairobi today: $temperature°C, rain ${String.format("%.1f", rainMm)} mm ($chance% chance)\n$advice"
    }
}