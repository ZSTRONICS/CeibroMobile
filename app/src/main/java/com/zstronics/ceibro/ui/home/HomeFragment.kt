package com.zstronics.ceibro.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.databinding.FragmentHomeBinding
import com.zstronics.ceibro.ui.projects.adapter.AllProjectsAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment :
    BaseNavViewModelFragment<FragmentHomeBinding, IHome.State, HomeVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: HomeVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_home
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.viewAllProjectsBtn -> {  }
        }
    }


    @Inject
    lateinit var adapter: AllProjectsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView(adapter)

        viewModel.homeProjects.observe(viewLifecycleOwner) {
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
        mViewDataBinding.homeProjectRV.setLayoutManager(object : LinearLayoutManager(mViewDataBinding.homeProjectRV.context, HORIZONTAL, false) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                // force width of recyclerview
                lp.width = (width / 1.12).toInt()
                return true
            }
        })

        mViewDataBinding.homeProjectRV.adapter = adapter

        adapter.itemLongClickListener =
            { _: View, _: Int, data: AllProjectsResponse.Result.Projects ->
                //showChatActionSheet(data)
            }
    }



}