package com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward.adapter.section

import com.intrusoft.sectionedrecyclerview.Section
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections

class ConnectionsSectionHeader constructor(
    private val childList: MutableList<AllCeibroConnections.CeibroConnection>, private val sectionText: String
) : Section<AllCeibroConnections.CeibroConnection> {

    override fun getChildItems(): MutableList<AllCeibroConnections.CeibroConnection> {
        return childList
    }
    fun getSectionText(): String {
        return sectionText
    }
}