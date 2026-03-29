package com.meghraj.zoneSync.utils

import com.meghraj.zoneSync.models.TimezoneData
import java.time.ZoneId
import java.util.Locale

object TimezoneHelper {

    fun getAllTimezones(): List<TimezoneData> {
        val availableIds = ZoneId.getAvailableZoneIds()
        val list = mutableListOf<TimezoneData>()
        
        for (id in availableIds) {
            val zoneId = ZoneId.of(id)
            if (id.contains("/")) {
                val parts = id.split("/")
                val city = parts.last().replace("_", " ")

                // Fetch real ISO country code via Android ICU library
                val regionCode = android.icu.util.TimeZone.getRegion(id)
                val countryName = try {
                    if (regionCode != null && regionCode != "001" && regionCode.length == 2) {
                        Locale.Builder().setRegion(regionCode).build().displayCountry
                    } else {
                        parts[0].replace("_", " ") // Fallback to region
                    }
                } catch (e: Exception) {
                    parts[0].replace("_", " ")
                }
                
                val flag = countryCodeToEmoji(regionCode ?: "001")
                
                list.add(TimezoneData(id, city, countryName, flag, zoneId))
            } else if (id == "UTC" || id == "GMT") {
                list.add(TimezoneData(id, id, "Global", "🌐", zoneId))
            }
        }
        
        return list.sortedBy { it.cityName }
    }

    private fun countryCodeToEmoji(countryCode: String): String {
        if (countryCode.length != 2) return "🌐"
        try {
            val uppercase = countryCode.uppercase()
            val firstLetter = Character.codePointAt(uppercase, 0) - 0x41 + 0x1F1E6
            val secondLetter = Character.codePointAt(uppercase, 1) - 0x41 + 0x1F1E6
            return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
        } catch (e: Exception) {
            return "🌐"
        }
    }
}
