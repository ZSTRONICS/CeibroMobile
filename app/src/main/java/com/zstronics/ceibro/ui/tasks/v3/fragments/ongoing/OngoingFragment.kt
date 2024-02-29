package com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentOngoingBinding
import com.zstronics.ceibro.ui.inbox.adapter.InboxAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OngoingFragment :
    BaseNavViewModelFragment<FragmentOngoingBinding, IOngoing.State, OngoingVM>() {


    @Inject
    lateinit var adapter: InboxAdapter
    private var fragmentManager: FragmentManager? = null
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: OngoingVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_ongoing
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {

            R.id.clTaskType -> {
                fragmentManager?.let {
                    chooseTaskType(it) { type ->
                        mViewDataBinding.tvtaskType.text= type

                    }
                }
            }

            R.id.imgSearch -> {
                mViewDataBinding.tasksSearchCard.visibility = View.VISIBLE
            }

            R.id.cancelSearch -> {
                mViewDataBinding.taskSearchBar.setQuery(null, false)
                mViewDataBinding.tasksSearchCard.visibility = View.GONE
            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentManager = childFragmentManager
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mViewDataBinding.taskRV.adapter = adapter

    }

    private fun chooseTaskType(fragmentManager: FragmentManager, callback: (String) -> Unit) {
        val sheet = TaskTypeBottomSheet {
            callback.invoke(it)
        }

        sheet.isCancelable = true
        sheet.show(fragmentManager, "TaskTypeBottomSheet")
    }
}