package com.zstronics.ceibro.ui.groupsv2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentInboxBinding
import com.zstronics.ceibro.ui.groupsv2.adapter.GroupV2Adapter
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject


@AndroidEntryPoint
class GroupV2Fragment :
    BaseNavViewModelFragment<FragmentInboxBinding, IGroupV2.State, GroupV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: GroupV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_group_v2
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.ivSort -> {

            }
        }
    }

    @Inject
    lateinit var adapter: GroupV2Adapter



    override fun onStart() {
        super.onStart()
        try {
            EventBus.getDefault().register(this)
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}