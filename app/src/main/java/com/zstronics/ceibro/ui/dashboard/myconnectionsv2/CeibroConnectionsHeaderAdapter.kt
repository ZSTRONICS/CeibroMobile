package com.zstronics.ceibro.ui.dashboard.myconnectionsv2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.databinding.LayoutItemConnectionHeaderBinding
import javax.inject.Inject

class CeibroConnectionsHeaderAdapter @Inject constructor() :
    RecyclerView.Adapter<CeibroConnectionsHeaderAdapter.CeibroConnectionsHeaderViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: AllCeibroConnections.CeibroConnection) -> Unit)? =
        null
    var listItems: MutableList<MyConnectionV2VM.CeibroConnectionGroup> = mutableListOf()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CeibroConnectionsHeaderViewHolder {
        return CeibroConnectionsHeaderViewHolder(
            LayoutItemConnectionHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CeibroConnectionsHeaderViewHolder, position: Int) {
        holder.bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: List<MyConnectionV2VM.CeibroConnectionGroup>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class CeibroConnectionsHeaderViewHolder(private val binding: LayoutItemConnectionHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MyConnectionV2VM.CeibroConnectionGroup) {
            binding.headerTitle.text = item.sectionLetter.toString()
            val adapter = CeibroConnectionsAdapter()
            adapter.setList(item.items)
            binding.connectionRV.adapter = adapter
            adapter.itemClickListener =
                { it: View, position: Int, data: AllCeibroConnections.CeibroConnection ->
                    itemClickListener?.invoke(it, absoluteAdapterPosition, data)
                }
        }
    }
}