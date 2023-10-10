package com.zstronics.ceibro.ui.dashboard

object TaskEventsList {

    val list = mutableListOf<Pair<String, String>>()

    private fun addEvent(eventType: String, value: String) {
        list.add(eventType to value)
    }

    fun removeEvent(eventType: String, value: String) {
        list.remove(eventType to value)
    }

    fun isExists(eventType: String, value: String, upsert: Boolean = false): Boolean {
        val found = list.filter { it.first == eventType && it.second == value }
        if (found.isEmpty() && upsert) {
            addEvent(eventType, value)
        }
        return found.isNotEmpty()
    }
}