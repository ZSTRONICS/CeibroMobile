package com.zstronics.ceibro.ui.tasks.v2.newtask.tag.adapter

import com.intrusoft.sectionedrecyclerview.Section
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse


class TagsDrawingSectionHeader(
    private val childList: List<TopicsResponse.TopicData>,
    private val sectionText: String,
) : Section<TopicsResponse.TopicData> {

    override fun getChildItems(): List<TopicsResponse.TopicData> {
        return childList
    }

    fun getSectionText(): String {
        return sectionText
    }
}