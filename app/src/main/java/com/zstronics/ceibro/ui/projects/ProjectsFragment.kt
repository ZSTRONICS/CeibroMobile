package com.zstronics.ceibro.ui.projects

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.chat.room.ChatRoom
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.databinding.FragmentProjectsBinding
import com.zstronics.ceibro.ui.chat.adapter.ChatRoomAdapter
import com.zstronics.ceibro.ui.projects.adapter.AllProjectsAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ProjectsFragment :
    BaseNavViewModelFragment<FragmentProjectsBinding, IProjects.State, ProjectsVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ProjectsVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_projects
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.projectFilterBtn -> {
                if (mViewDataBinding.filterScrollLayout.visibility == View.VISIBLE) {
                    mViewDataBinding.filterScrollLayout.moveView(250, false)
                }
                else {
                    mViewDataBinding.filterScrollLayout.moveView(300, true)
                }

            }
        }
    }

    @Inject
    lateinit var adapter: AllProjectsAdapter

    private fun View.moveView(duration: Long, isVisible: Boolean) {
        val transition: Transition = Slide(Gravity.TOP)
        transition.duration = duration
        transition.addTarget(this)
        TransitionManager.beginDelayedTransition(this.parent as ViewGroup, transition)
        this.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView(adapter)

        viewModel.allProjects.observe(viewLifecycleOwner) {
            adapter.setList(it)
        }
        adapter.itemClickListener = { _: View, position: Int, data: AllProjectsResponse.Result.Projects ->
            //navigateToMsgView(data)
        }
        adapter.childItemClickListener = { view: View, position: Int, data: AllProjectsResponse.Result.Projects ->
            //if (view.id == R.id.chatFavIcon)
                //viewModel.addChatToFav(data.id)
        }
    }

    private fun initRecyclerView(adapter: AllProjectsAdapter) {
        mViewDataBinding.projectRV.adapter = adapter

        adapter.itemLongClickListener =
            { _: View, _: Int, data: AllProjectsResponse.Result.Projects ->
                //showChatActionSheet(data)
            }
    }


}