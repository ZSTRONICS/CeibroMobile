package com.zstronics.ceibro.utils

import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import com.zstronics.ceibro.data.repos.chat.room.Member


object CommonBinding {

    @JvmStatic
    @BindingAdapter(value = ["app:participants"], requireAll = true)
    fun setParticipantsText(
        textView: AppCompatTextView,
        participants: ArrayList<Member>?,
    ) {
        val participantsNames = participants?.filter { it.isChecked }

        textView.text = when {
            participants?.isEmpty() == true -> "Choose participants"
            else -> {
                var names = ""
                participantsNames?.map {
                    names += "${it.firstName},"
                }
                names.dropLast(1)
            }
        }
    }
}