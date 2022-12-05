package com.zstronics.ceibro.ui.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutItemChatMembersBinding
import javax.inject.Inject

class ChatMembersAdapter @Inject constructor(val sessionManager: SessionManager) :
    RecyclerView.Adapter<ChatMembersAdapter.ChaMembersViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: Member) -> Unit)? = null

    private var list: MutableList<Member> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChaMembersViewHolder {
        return ChaMembersViewHolder(
            LayoutItemChatMembersBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ChaMembersViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<Member>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class ChaMembersViewHolder(private val binding: LayoutItemChatMembersBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Member) {
            binding.member = item

            itemView.setOnClickListener {
                itemClickListener?.invoke(it, adapterPosition, item)
            }

        }
    }
}