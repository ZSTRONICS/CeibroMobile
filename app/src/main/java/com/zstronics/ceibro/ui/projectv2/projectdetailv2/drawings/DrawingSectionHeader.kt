package com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawings

import com.intrusoft.sectionedrecyclerview.Section
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2


class DrawingSectionHeader(
    private val childList: MutableList<CeibroGroupsV2>,
    private val sectionText: String,
) : Section<CeibroGroupsV2> {

    override fun getChildItems(): MutableList<CeibroGroupsV2> {
        return childList
    }

    fun getSectionText(): String {
        return sectionText
    }
}