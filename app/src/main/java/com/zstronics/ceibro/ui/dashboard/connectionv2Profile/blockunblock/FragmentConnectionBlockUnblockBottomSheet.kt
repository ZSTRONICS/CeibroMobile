package com.zstronics.ceibro.ui.dashboard.connectionv2Profile.blockunblock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.databinding.FragmentConnectionBlockUblockBinding
import com.zstronics.ceibro.ui.enums.ConnectionStatusActions

class FragmentConnectionBlockUnblockBottomSheet constructor(val action: ConnectionStatusActions) :
    BottomSheetDialogFragment(),
    View.OnClickListener {
    lateinit var binding: FragmentConnectionBlockUblockBinding

    var actionClick: ((view: View?, action: ConnectionStatusActions) -> Unit)? =
        null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_connection_block_ublock,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (action) {
            ConnectionStatusActions.BLOCK -> {
                binding.block.visibility = View.VISIBLE
                binding.unblock.visibility = View.GONE
            }
            ConnectionStatusActions.UNBLOCK -> {
                binding.block.visibility = View.GONE
                binding.unblock.visibility = View.VISIBLE
            }
        }

        binding.block.setOnClickListener(this)
        binding.unblock.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        val action: ConnectionStatusActions = when (p0) {
            binding.block -> ConnectionStatusActions.BLOCK
            binding.unblock -> ConnectionStatusActions.UNBLOCK
            else -> ConnectionStatusActions.UNBLOCK
        }
        actionClick?.invoke(p0, action)
    }
}