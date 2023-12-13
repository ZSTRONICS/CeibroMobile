package com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawing

import com.intrusoft.sectionedrecyclerview.Section


class DrawingSectionHeader(
    private val childList: List<StringListData>,
    private val sectionText: String,
) : Section<StringListData> {

    override fun getChildItems(): List<StringListData> {
        return childList
    }

    fun getSectionText(): String {
        return sectionText
    }
}