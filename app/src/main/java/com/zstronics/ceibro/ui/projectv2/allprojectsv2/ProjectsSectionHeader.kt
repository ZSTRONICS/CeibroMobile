package com.zstronics.ceibro.ui.projectv2.allprojectsv2

import com.intrusoft.sectionedrecyclerview.Section
import com.zstronics.ceibro.data.repos.chat.room.Project


class ProjectsSectionHeader(
    private val childList: MutableList<Project>,
    private val sectionText: String,
) : Section<Project> {

    override fun getChildItems(): MutableList<Project> {
        return childList
    }

    fun getSectionText(): String {
        return sectionText
    }
}
