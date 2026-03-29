package com.meghraj.zoneSync.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.meghraj.zoneSync.data.DataStoreManager
import com.meghraj.zoneSync.models.TimezoneData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = DataStoreManager(application)

    private val _timezones = MutableStateFlow<List<TimezoneData>>(emptyList())
    val timezones: StateFlow<List<TimezoneData>> = _timezones.asStateFlow()

    private val _referenceClockId = MutableStateFlow<String?>(null)
    val referenceClockId: StateFlow<String?> = _referenceClockId.asStateFlow()

    private val _referenceTime = MutableStateFlow<LocalDateTime?>(null)
    val referenceTime: StateFlow<LocalDateTime?> = _referenceTime.asStateFlow()

    private val _currentTime = MutableStateFlow(LocalDateTime.now())
    val currentTime: StateFlow<LocalDateTime> = _currentTime.asStateFlow()

    private var recentlyDeletedItem: TimezoneData? = null
    private var recentlyDeletedIndex: Int? = null

    init {
        viewModelScope.launch {
            dataStore.savedTimezones.collect { saved ->
                _timezones.value = saved
            }
        }
        
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _currentTime.value = LocalDateTime.now()
            }
        }
    }

    fun addTimezone(timezone: TimezoneData) {
        val currentList = _timezones.value.toMutableList()
        if (!currentList.any { it.id == timezone.id }) {
            currentList.add(timezone)
            updateAndSave(currentList)
        }
    }

    fun removeTimezone(timezone: TimezoneData) {
        val currentList = _timezones.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == timezone.id }
        if (index != -1) {
            recentlyDeletedItem = currentList[index]
            recentlyDeletedIndex = index
            currentList.removeAt(index)
            
            if (_referenceClockId.value == timezone.id) {
                clearReference()
            }
            updateAndSave(currentList)
        }
    }

    fun undoDelete() {
        val item = recentlyDeletedItem
        val index = recentlyDeletedIndex
        if (item != null && index != null) {
            val currentList = _timezones.value.toMutableList()
            currentList.add(index.coerceIn(0, currentList.size), item)
            updateAndSave(currentList)
            recentlyDeletedItem = null
            recentlyDeletedIndex = null
        }
    }

    fun setReferenceTime(id: String, time: LocalDateTime) {
        _referenceClockId.value = id
        _referenceTime.value = time
    }

    fun clearReference() {
        _referenceClockId.value = null
        _referenceTime.value = null
    }

    fun reorderTimezones(fromIndex: Int, toIndex: Int) {
        val currentList = _timezones.value.toMutableList()
        val item = currentList.removeAt(fromIndex)
        currentList.add(toIndex, item)
        _timezones.value = currentList
        viewModelScope.launch {
            dataStore.saveTimezone(currentList)
        }
    }

    private fun updateAndSave(list: List<TimezoneData>) {
        _timezones.value = list
        viewModelScope.launch {
            dataStore.saveTimezone(list)
        }
    }

    fun getCalculatedTimeFor(zoneIdStr: String): Pair<LocalDateTime, Boolean> {
        val zoneId = ZoneId.of(zoneIdStr)
        val refIdStr = _referenceClockId.value
        val refTime = _referenceTime.value

        if (refIdStr != null && refTime != null) {
            val refZoneId = ZoneId.of(refIdStr)
            val zonedDateTime = refTime.atZone(refZoneId)
            val targetZonedTime = zonedDateTime.withZoneSameInstant(zoneId)
            return Pair(targetZonedTime.toLocalDateTime(), false)
        } else {
            val liveTime = _currentTime.value.atZone(ZoneId.systemDefault()).withZoneSameInstant(zoneId).toLocalDateTime()
            return Pair(liveTime, true)
        }
    }
}
