package com.zstronics.ceibro.ui.projects.newproject.overview.ownersheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.dashboard.connections.MyConnection
import com.zstronics.ceibro.data.repos.dashboard.connections.toMember
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.FragmentOwnersSelectionBinding

class OwnerSelectionSheet constructor(
    private val connections: ArrayList<MyConnection>?,
    val sessionManager: SessionManager,
    val owners: LiveData<ArrayList<String>>
) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentOwnersSelectionBinding
    var onSelect: ((member: Member) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_owners_selection,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = OwnerSelectionAdapter()
        binding.connectionRV.adapter = adapter
        connections?.let {
            adapter.setList(connections)
        }
        adapter.itemClickListener =
            { childView: View, position: Int, member: Member ->
                onSelect?.invoke(member)
            }
        binding.closeBtn.setOnClickListener {
            dismiss()
        }
        owners.observe(viewLifecycleOwner) {
            adapter.setSelectedConnection(it)
        }
    }
}