package com.example.communityinfrastructurereportingsystem.data.model

// The app only works inside Nairobi County
object NairobiConfig {

    // Nairobi's 17 sub-counties (shown in a dropdown on the report form)
    val subCounties = listOf(
        "Westlands", "Dagoretti North", "Dagoretti South", "Langata", "Kibra",
        "Roysambu", "Kasarani", "Ruaraka", "Embakasi South", "Embakasi North",
        "Embakasi Central", "Embakasi East", "Embakasi West", "Makadara",
        "Kamukunji", "Starehe", "Mathare"
    )

    // Approximate box around Nairobi County
    const val MIN_LAT = -1.45
    const val MAX_LAT = -1.16
    const val MIN_LNG = 36.65
    const val MAX_LNG = 37.10

    // Centre of Nairobi, used later for the weather API
    const val CENTER_LAT = -1.2921
    const val CENTER_LNG = 36.8219

    // True only when a GPS point falls inside the box above
    fun isInsideNairobi(lat: Double, lng: Double): Boolean =
        lat in MIN_LAT..MAX_LAT && lng in MIN_LNG..MAX_LNG
}