package com.zstronics.ceibro.ui.tasks.v2.newtask.taskproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.databinding.LayoutItemTopicBinding
import java.util.Locale
import javax.inject.Inject

class AllProjectsAdapter @Inject constructor() :
    RecyclerView.Adapter<AllProjectsAdapter.AllProjectsViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: CeibroProjectV2) -> Unit)? =
        null
    var listItems: MutableList<CeibroProjectV2> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllProjectsViewHolder {
        return AllProjectsViewHolder(
            LayoutItemTopicBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AllProjectsViewHolder, position: Int) {
        holder.bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: List<CeibroProjectV2>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class AllProjectsViewHolder(private val binding: LayoutItemTopicBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CeibroProjectV2) {

            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
            val context = binding.menuBtn.context

            binding.topicName.text = item.title.capitalize(Locale.ROOT)
            binding.menuBtn.visibility = View.GONE
        }
    }
}