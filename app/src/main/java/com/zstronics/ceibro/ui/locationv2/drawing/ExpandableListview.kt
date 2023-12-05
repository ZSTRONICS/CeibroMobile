package com.zstronics.ceibro.ui.locationv2.drawing

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView

class ExpandableListAdapter(
    private val context: Context,
    private val groupData: List<String>,
    private val childData: Map<String, List<String>>
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int = groupData.size

    override fun getChildrenCount(groupPosition: Int): Int = childData[groupData[groupPosition]]?.size ?: 0

    override fun getGroup(groupPosition: Int): Any = groupData[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any =
        childData[groupData[groupPosition]]?.get(childPosition) ?: ""

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun hasStableIds(): Boolean = true

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_expandable_list_item_1, parent, false)
        val textView: TextView = view.findViewById(android.R.id.text1)
        textView.text = getGroup(groupPosition).toString()
        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_expandable_list_item_2, parent, false)
        val textView: TextView = view.findViewById(android.R.id.text1)
        textView.text = getChild(groupPosition, childPosition).toString()
        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
}
