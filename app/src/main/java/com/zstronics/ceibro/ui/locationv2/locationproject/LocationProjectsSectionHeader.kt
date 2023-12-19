package com.zstronics.ceibro.ui.locationv2.locationproject

import com.intrusoft.sectionedrecyclerview.Section
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2


class LocationProjectsSectionHeader(
    private val childList: MutableList<CeibroProjectV2>,
    private val sectionText: String,
) : Section<CeibroProjectV2> {

    override fun getChildItems(): MutableList<CeibroProjectV2> {
        return childList
    }

    fun getSectionText(): String {
        return sectionText
    }
}
