package com.meghraj.zoneSync.models

import java.time.ZoneId

data class TimezoneData(
    val id: String, // typically the ZoneId string representation
    val cityName: String,
    val countryName: String,
    val flagEmoji: String,
    val zoneId: ZoneId
) {
    companion object {
        fun fromId(id: String, cityName: String, countryName: String, flagEmoji: String): TimezoneData {
            return TimezoneData(
                id = id,
                cityName = cityName,
                countryName = countryName,
                flagEmoji = flagEmoji,
                zoneId = ZoneId.of(id)
            )
        }
    }
}
