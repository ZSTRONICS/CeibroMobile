package com.zstronics.ceibro.ui.tasks.subtaskcomments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.subtask.SubTaskComments
import com.zstronics.ceibro.data.repos.dashboard.attachment.AttachmentModules
import com.zstronics.ceibro.databinding.FragmentSubTaskCommentsBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import com.zstronics.ceibro.ui.tasks.subtaskdetailview.CommentsAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SubTaskCommentsFragment :
    BaseNavViewModelFragment<FragmentSubTaskCommentsBinding, ISubTaskComments.State, SubTaskCommentsVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: SubTaskCommentsVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_sub_task_comments
    override fun toolBarVisibility(): Boolean = false

    @Inject
    lateinit var commentsAdapter: CommentsAdapter
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> navigateBack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.subTaskAllCommentsRV.adapter = commentsAdapter
        commentsAdapter.itemClickListener =
            { _: View, position: Int, data: SubTaskComments ->
                arguments?.putString("moduleType", AttachmentModules.SubTaskComments.name)
                arguments?.putString("moduleId", data.id)
                arguments?.putParcelable("SubTaskComments", data)
                navigate(R.id.attachmentFragment, arguments)
            }
        viewModel.allComment.observe(viewLifecycleOwner) { comments ->
            commentsAdapter.setList(comments)
        }
    }
}