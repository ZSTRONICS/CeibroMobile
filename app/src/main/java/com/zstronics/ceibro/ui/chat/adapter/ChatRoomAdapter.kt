package com.zstronics.ceibro.ui.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.repos.chat.room.ChatRoom
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutChatBoxBinding
import com.zstronics.ceibro.utils.DateUtils
import javax.inject.Inject

class ChatRoomAdapter @Inject constructor(val sessionManager: SessionManager) :
    RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder>() {
    val user = sessionManager.getUser().value
    var itemClickListener: ((view: View, position: Int, data: ChatRoom) -> Unit)? = null
    var itemLongClickListener: ((view: View, position: Int, data: ChatRoom) -> Unit)? =
        null
    var childItemClickListener: ((view: View, position: Int, data: ChatRoom) -> Unit)? = null

    private var list: MutableList<ChatRoom> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        return ChatRoomViewHolder(
            LayoutChatBoxBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<ChatRoom>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class ChatRoomViewHolder(private val binding: LayoutChatBoxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatRoom) {
            val context = binding.root.context
            val otherUser: Member? = item.members.find { member -> member.id != user?.id }
            binding.chatRoom = item

            binding.chatProfileIconText.text = ""

            if (item.isGroupChat) {
                binding.chatProjectName.text = item.project?.title
                binding.chatPersonName.text = item.name
                val groupName = item.name.split("\\s".toRegex()).toTypedArray()
                for (element in groupName) {
                    binding.chatProfileIconText.append("${element[0]?.uppercaseChar()}")
                }
                binding.chatTypeIcon.setImageResource(R.drawable.icon_group_chat)
            } else {
                binding.chatPersonName.text = "${otherUser?.firstName} ${otherUser?.surName}"
                binding.chatProfileIconText.text =
                    "${otherUser?.firstName?.get(0)?.uppercaseChar()}${
                        otherUser?.surName?.get(0)?.uppercaseChar()
                    }"
                binding.chatTypeIcon.setImageResource(R.drawable.icon_individual_chat)
            }

            if (item.lastMessage?.message == null || item.lastMessage?.message == "") {
                binding.chatText.text = context.resources.getString(R.string.no_conversation)
            } else {
                binding.chatText.text = item.lastMessage?.message
            }


            val isFavFound = item.pinnedBy.find { it == user?.id }
            val favId =
                if (isFavFound != null) R.drawable.icon_star_filled else R.drawable.icon_star_outline
            binding.chatFavIcon.setImageResource(favId)

            binding.msgTimeText.text = DateUtils.reformatStringDate(
                item.updatedAt, DateUtils.SERVER_DATE_FULL_FORMAT, DateUtils.FORMAT_TIME_24H
            )

            itemView.setOnClickListener {
                itemClickListener?.invoke(it, adapterPosition, item)
            }

            itemView.setOnLongClickListener {
                itemLongClickListener?.invoke(it, adapterPosition, item)
                true
            }

            binding.chatFavIcon.setOnClickListener {
                val userId: String? = item.pinnedBy.find { userId -> userId == user?.id }
                if (userId != null) item.pinnedBy.remove(userId) else item.pinnedBy.add(user?.id.toString())
                notifyItemChanged(list.indexOf(item))
                childItemClickListener?.invoke(it, adapterPosition, item)
            }

        }
    }
}