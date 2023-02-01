package com.zstronics.ceibro.ui.tasks.subtaskdetailview

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentSubTaskDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubTaskDetailFragment :
    BaseNavViewModelFragment<FragmentSubTaskDetailBinding, ISubTaskDetail.State, SubTaskDetailVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: SubTaskDetailVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_sub_task_detail
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> navigateBack()
            R.id.subTaskViewCommentsBtn -> navigateToAllComments()
            R.id.subTaskDescriptionShowMoreBtn -> {
                if (mViewDataBinding.subTaskDescriptionText.maxLines == 4) {
                    mViewDataBinding.subTaskDescriptionText.maxLines = Integer.MAX_VALUE
                    mViewDataBinding.subTaskDescriptionShowMoreBtn.text = resources.getString(R.string.show_less_heading)
                } else {
                    mViewDataBinding.subTaskDescriptionText.maxLines = 4
                    mViewDataBinding.subTaskDescriptionShowMoreBtn.text = resources.getString(R.string.show_more_heading)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        with(mViewDataBinding) {
            subTaskDescriptionText.text = "Any description of this subtask can be displayed over here. Any description of this subtask can be displayed over here. " +
                    "Any description of this subtask can be displayed over here. Any description of this subtask can be displayed over here. " +
                    "Any description of this subtask can be displayed over here. Any description of this subtask can be displayed over here. " +
                    "Any description of this subtask can be displayed over here. Any description of this subtask can be displayed over here. " +
                    "Any description of this subtask can be displayed over here. Any description of this subtask can be displayed over here. " +
                    "Any description of this subtask can be displayed over here. Any description of this subtask can be displayed over here. " +
                    "Any description of this subtask can be displayed over here. Any description of this subtask can be displayed over here. " +
                    "Any description of this subtask can be displayed over here. Any description of this subtask can be displayed over here. "

            subTaskDescriptionText.post(Runnable {
                if (subTaskDescriptionText.lineCount <= 4) {
                    subTaskDescriptionShowMoreBtn.visibility = View.GONE
                } else {
                    subTaskDescriptionShowMoreBtn.visibility = View.VISIBLE
                }
            })

            subTaskCommentField.setOnTouchListener { view, event ->
                view.parent.requestDisallowInterceptTouchEvent(true)
                if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                    view.parent.requestDisallowInterceptTouchEvent(false)
                }
                return@setOnTouchListener false
            }
        }
    }

    private fun navigateToAllComments() {
        navigate(R.id.subTaskCommentsFragment)
    }
}