package com.zstronics.ceibro.ui.groupsv2.adapter

import com.intrusoft.sectionedrecyclerview.Section
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2


class GroupSectionHeader(
    private val childList: MutableList<CeibroConnectionGroupV2>,
    private val sectionText: String,
) : Section<CeibroConnectionGroupV2> {

    override fun getChildItems(): MutableList<CeibroConnectionGroupV2> {
        return childList
    }

    fun getSectionText(): String {
        return sectionText
    }
}