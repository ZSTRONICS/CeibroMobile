package com.zstronics.ceibro.ui.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.chat.messages.MessagesResponse
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutItemChatMembersBinding
import com.zstronics.ceibro.databinding.LayoutItemMessageInfoReadByBinding
import javax.inject.Inject

class MessageInfoReadByAdapter @Inject constructor(val sessionManager: SessionManager) :
    RecyclerView.Adapter<MessageInfoReadByAdapter.MessageInfoReadByViewHolder>() {

    private var list: MutableList<MessagesResponse.ChatMessage.ReadBy> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageInfoReadByViewHolder {
        return MessageInfoReadByViewHolder(
            LayoutItemMessageInfoReadByBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MessageInfoReadByViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<MessagesResponse.ChatMessage.ReadBy>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class MessageInfoReadByViewHolder(private val binding: LayoutItemMessageInfoReadByBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MessagesResponse.ChatMessage.ReadBy) {
            binding.readBy = item

        }
    }
}