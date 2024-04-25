package com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward.adapter.section

import com.intrusoft.sectionedrecyclerview.Section
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections

class GroupConnectionsSectionHeader constructor(
    private val childList: MutableList<TaskMemberDetail>,
    private val sectionText: String,
    val isDataLoading: Boolean = true
) : Section<TaskMemberDetail> {

    override fun getChildItems(): MutableList<TaskMemberDetail> {
        return childList
    }

    fun getSectionText(): String {
        return sectionText
    }
}