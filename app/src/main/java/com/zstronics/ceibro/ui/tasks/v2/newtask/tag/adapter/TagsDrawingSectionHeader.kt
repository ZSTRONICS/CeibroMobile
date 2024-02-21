package com.zstronics.ceibro.ui.tasks.v2.newtask.tag.adapter

import com.intrusoft.sectionedrecyclerview.Section
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2


class TagsDrawingSectionHeader(
    private val childList: MutableList<String>,
    private val sectionText: String,
) : Section<String> {

    override fun getChildItems(): MutableList<String> {
        return childList
    }

    fun getSectionText(): String {
        return sectionText
    }
}