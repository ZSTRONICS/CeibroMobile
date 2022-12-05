package com.zstronics.ceibro.base.validator.util

import android.view.View
import android.view.ViewGroup
import java.util.*

/**
 * Created by Muhammad Ibraheem
 */
object ViewTagHelper {
    fun <Type> appendValue(tagId: Int, view: View?, value: Type) {
        val `object` = view?.getTag(tagId)
        if (`object` is List<*>) {
            (`object` as MutableList<Type>).add(value)
        } else {
            val typeList: MutableList<Type> = ArrayList()
            typeList.add(value)
            view?.setTag(tagId, typeList)
        }
    }

    @JvmStatic
    fun getViewsByTag(
        root: ViewGroup,
        tagId: Int
    ): List<View> {
        val views: MutableList<View> =
            ArrayList()
        val childCount = root.childCount
        for (i in 0 until childCount) {
            val child = root.getChildAt(i)
            if (child is ViewGroup) {
                views.addAll(getViewsByTag(child, tagId))
            }
            addViewWhenContainsTag(tagId, views, child)
        }
        return views
    }

    @JvmStatic
    fun filterViewWithTag(
        tagId: Int,
        view: View
    ): List<View> {
        val viewsWithTags: MutableList<View> =
            ArrayList()
        addViewWhenContainsTag(tagId, viewsWithTags, view)
        return viewsWithTags
    }

    @JvmStatic
    fun <ViewType : View?> filterViewsWithTag(
        tagId: Int,
        views: List<ViewType>
    ): List<View> {
        val viewsWithTags: MutableList<View> =
            ArrayList()
        for (view in views) {
            addViewWhenContainsTag(tagId, viewsWithTags, view!!)
        }
        return viewsWithTags
    }

    private fun addViewWhenContainsTag(
        tagId: Int,
        views: MutableList<View>,
        view: View
    ) {
        val tagValue = view.getTag(tagId)
        if (tagValue != null) {
            views.add(view)
        }
    }
}