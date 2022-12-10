package com.zstronics.ceibro.ui.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.chat.messages.MessagesResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutItemMessageReceiveBinding
import com.zstronics.ceibro.databinding.LayoutItemMessageSenderBinding
import com.zstronics.ceibro.ui.enums.MessageType
import javax.inject.Inject

class MessagesAdapter @Inject constructor(val sessionManager: SessionManager) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val user = sessionManager.getUser().value
    var itemClickListener: ((view: View, position: Int, data: MessagesResponse.ChatMessage) -> Unit)? =
        null
    var quotedClickListener: ((view: View, position: Int, data: MessagesResponse.ReplyOf?) -> Unit)? =
        null
    var itemLongClickListener: ((view: View, position: Int, data: MessagesResponse.ChatMessage) -> Unit)? =
        null
    var quickReplyClick: ((view: View, position: Int, data: MessagesResponse.ChatMessage, messageBody: String) -> Unit)? =
        null
    private var list: MutableList<MessagesResponse.ChatMessage> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == MY_MESSAGE) {
            MyMessagesViewHolder(
                LayoutItemMessageSenderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

        } else {
            OtherMessagesViewHolder(
                LayoutItemMessageReceiveBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyMessagesViewHolder) holder.bind(list[position])
        else if (holder is OtherMessagesViewHolder) holder.bind(list[position])
    }

    private val MY_MESSAGE = 1
    private val OTHER_MESSAGE = 2

    override fun getItemViewType(position: Int): Int {
        return if (list[position].myMessage == sessionManager.getUser().value?.id) {
            MY_MESSAGE
        } else {
            OTHER_MESSAGE
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<MessagesResponse.ChatMessage>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun appendMessage(
        message: MessagesResponse.ChatMessage, callback: (lastPosition: Int) -> Unit
    ) {
        list.add(message)
        val lastPosition = list.size.minus(1)
        notifyItemInserted(lastPosition)
        callback.invoke(lastPosition)
    }

    inner class MyMessagesViewHolder(private val binding: LayoutItemMessageSenderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MessagesResponse.ChatMessage) {
            binding.message = item

            binding.seenCount.visibility = if (item.readBy !== null) {
                binding.seenCount.text = item.readBy.size.toString()
                View.VISIBLE
            } else
                View.GONE

            if (item.replyOf != null) {

                binding.quotedMessageLayout.visibility = View.VISIBLE
                binding.quotedUser.text = "Username"
                binding.quotedMessage.text = item.replyOf?.message
            } else {
                binding.quotedMessageLayout.visibility = View.GONE
                binding.quotedUser.text = ""
                binding.quotedMessage.text = ""
            }

            if (item.type == MessageType.QUESTIONIAR.name.lowercase()) {
                binding.questionLayout.visibility = View.VISIBLE
                binding.senderMsgText.visibility = View.GONE
            } else {
                binding.questionLayout.visibility = View.GONE
                binding.senderMsgText.visibility = View.VISIBLE
            }

            itemView.setOnClickListener {
                itemClickListener?.invoke(it, adapterPosition, item)
            }

            binding.quotedMessageLayout.setOnClickListener {
                quotedClickListener?.invoke(it, adapterPosition, item.replyOf)
            }

            itemView.setOnLongClickListener {
                itemLongClickListener?.invoke(it, adapterPosition, item)
                true
            }
        }
    }

    inner class OtherMessagesViewHolder(private val binding: LayoutItemMessageReceiveBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MessagesResponse.ChatMessage) {
            binding.message = item

            binding.seenCount.visibility = if (item.readBy !== null) {
                binding.seenCount.text = item.readBy.size.toString()
                View.VISIBLE
            } else
                View.GONE

            if (item.replyOf != null) {
                binding.quotedMessageLayout.visibility = View.VISIBLE
                binding.quotedUser.text = "Username"
                binding.quotedMessage.text = item.replyOf?.message
            } else {
                binding.quotedMessageLayout.visibility = View.GONE
                binding.quotedUser.text = ""
                binding.quotedMessage.text = ""
            }

            if (item.type == MessageType.QUESTIONIAR.name.lowercase()) {
                binding.questionLayout.visibility = View.VISIBLE
                binding.senderMsgText.visibility = View.GONE
            } else {
                binding.questionLayout.visibility = View.GONE
                binding.senderMsgText.visibility = View.VISIBLE
            }

            itemView.setOnClickListener {
                itemClickListener?.invoke(it, adapterPosition, item)
            }

            binding.quotedMessageLayout.setOnClickListener {
                quotedClickListener?.invoke(it, adapterPosition, item.replyOf)
            }
            itemView.setOnLongClickListener {
                itemLongClickListener?.invoke(it, adapterPosition, item)
                true
            }

            binding.quickReplyDone.setOnClickListener {
                quickReplyClick?.invoke(it, adapterPosition, item, "Done")
            }

            binding.quickReplyOk.setOnClickListener {
                quickReplyClick?.invoke(it, adapterPosition, item, "Ok")
            }

            binding.quickReplyQuestionMark.setOnClickListener {
                quickReplyClick?.invoke(it, adapterPosition, item, "?")
            }
        }
    }
}
//              binding.senderMsgTime.text = DateUtils.reformatStringDate(
//                item.createdAt, DateUtils.SERVER_DATE_FULL_FORMAT, DateUtils.FORMAT_TIME_12H
//            )