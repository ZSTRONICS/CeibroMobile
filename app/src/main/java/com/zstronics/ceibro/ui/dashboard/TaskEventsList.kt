package com.zstronics.ceibro.ui.dashboard

object TaskEventsList {
    private val keyValueMap = HashMap<String, Boolean>()

    private fun addEvent(eventType: String, value: String) {
        if (!keyValueMap.contains(eventType + '_' + value)) {
            keyValueMap[eventType + '_' + value] = true
        }
    }

    fun removeEvent(eventType: String, value: String) {
        if (keyValueMap.contains(eventType + '_' + value)) {
            keyValueMap.remove(eventType + '_' + value)
        }
    }

    fun isExists(eventType: String, value: String, upsert: Boolean = false): Boolean {
        val found = keyValueMap.contains(eventType + '_' + value)
        if (!found && upsert) {
            addEvent(eventType, value)
        }
        return found
    }
}