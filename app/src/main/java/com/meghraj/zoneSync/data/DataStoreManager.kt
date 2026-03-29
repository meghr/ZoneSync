package com.meghraj.zoneSync.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import com.meghraj.zoneSync.models.TimezoneData

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {

    private val TIMEZONES_KEY = stringPreferencesKey("saved_timezones")

    val savedTimezones: Flow<List<TimezoneData>> = context.dataStore.data
        .map { preferences ->
            val jsonString = preferences[TIMEZONES_KEY] ?: "[]"
            parseTimezones(jsonString)
        }

    suspend fun saveTimezone(list: List<TimezoneData>) {
        context.dataStore.edit { preferences ->
            preferences[TIMEZONES_KEY] = serializeTimezones(list)
        }
    }

    private fun parseTimezones(jsonString: String): List<TimezoneData> {
        val list = mutableListOf<TimezoneData>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TimezoneData.fromId(
                        id = obj.getString("id"),
                        cityName = obj.getString("cityName"),
                        countryName = obj.getString("countryName"),
                        flagEmoji = obj.getString("flagEmoji")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun serializeTimezones(list: List<TimezoneData>): String {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("cityName", item.cityName)
            obj.put("countryName", item.countryName)
            obj.put("flagEmoji", item.flagEmoji)
            array.put(obj)
        }
        return array.toString()
    }
}
