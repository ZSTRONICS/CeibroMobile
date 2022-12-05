package com.zstronics.ceibro.ui.questioner.createquestion.members

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.databinding.FragmentParticipantsBinding

class FragmentQuestionParticipantsSheet constructor(val listOfParticipants: ArrayList<Member>) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentParticipantsBinding

    var onDoneClick: ((view: View?, dataList: ArrayList<Member>) -> Unit)? =
        null

    lateinit var adapter: ParticipantsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_participants,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ParticipantsAdapter()

        adapter.setList(listOfParticipants)

        binding.recyclerView.adapter = adapter
        binding.closeBtn.setOnClickListener {
            dismiss()
        }

        binding.doneBtn.setOnClickListener {
            onDoneClick?.invoke(it, adapter.dataList)
            dismiss()
        }
    }
}