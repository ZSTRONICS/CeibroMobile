package com.zstronics.ceibro.ui.inbox.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutTaskBoxV2Binding
import javax.inject.Inject

class InboxAdapter @Inject constructor() :
    RecyclerView.Adapter<InboxAdapter.TaskToMeViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: CeibroTaskV2) -> Unit)? =
        null
    var itemLongClickListener: ((view: View, position: Int, data: CeibroTaskV2) -> Unit)? =
        null
    var listItems: MutableList<CeibroTaskV2> = mutableListOf()
    var currentUser = SessionManager.user.value
    var sessionManager: SessionManager? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskToMeViewHolder {
        return TaskToMeViewHolder(
            LayoutTaskBoxV2Binding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TaskToMeViewHolder, position: Int) {
        holder.bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: List<CeibroTaskV2>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class TaskToMeViewHolder(private val binding: LayoutTaskBoxV2Binding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CeibroTaskV2?) {
            val context = binding.root.context

            binding.apply {

            }.lifecycleOwner

        }
    }
}