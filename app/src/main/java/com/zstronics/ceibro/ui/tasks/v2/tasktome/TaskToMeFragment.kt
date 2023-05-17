package com.zstronics.ceibro.ui.tasks.v2.tasktome

import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentTaskToMeBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskToMeFragment :
    BaseNavViewModelFragment<FragmentTaskToMeBinding, ITaskToMe.State, TaskToMeVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskToMeVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_to_me
    override fun toolBarVisibility(): Boolean = false
    var buttonOnSide = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.createNewTaskBtn -> {
                navigate(R.id.newTaskV2Fragment)
//                if (!buttonOnSide) {
//                    // If the sidebar is visible, hide it with animation
//                    buttonOnSide = true
//                    mViewDataBinding.createTaskBtn.animate()
//                        .translationX(mViewDataBinding.createTaskBtn.width.toFloat()-20)
//                        .setDuration(350)
//                        .withEndAction { mViewDataBinding.createTaskBtn.visibility = View.VISIBLE }
//                        .start()
//                } else {
//                    // If the sidebar is hidden, show it with animation
//                    buttonOnSide = false
//                    mViewDataBinding.createTaskBtn.visibility = View.VISIBLE
//                    mViewDataBinding.createTaskBtn.animate()
//                        .translationX(0f)
//                        .setDuration(350)
//                        .start()
//                }
            }
        }
    }
}