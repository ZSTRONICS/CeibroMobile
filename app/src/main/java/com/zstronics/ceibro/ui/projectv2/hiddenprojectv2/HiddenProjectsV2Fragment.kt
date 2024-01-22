package com.zstronics.ceibro.ui.projectv2.hiddenprojectv2

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.databinding.FragmentHiddenProjectsV2Binding
import com.zstronics.ceibro.ui.dashboard.SharedViewModel
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject


@AndroidEntryPoint
class HiddenProjectsV2Fragment :
    BaseNavViewModelFragment<FragmentHiddenProjectsV2Binding, IHiddenProjectV2.State, HiddenProjectsV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: HiddenProjectsV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_hidden_projects_v2
    override fun toolBarVisibility(): Boolean = false
    var firstStartOffragment = true
    var sharedViewModel: SharedViewModel? = null
    override fun onClick(id: Int) {
        when (id) {

        }
    }

    @Inject
    lateinit var adapter: HiddenProjectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        mViewDataBinding.projectHiddenHeader.visibility = View.GONE
        mViewDataBinding.projectsRV.visibility = View.VISIBLE

        mViewDataBinding.projectsRV.adapter = adapter

        initRecyclerView(adapter)
        viewModel.allHiddenProjects.observe(viewLifecycleOwner) {

            if (it.isNotEmpty()) {
                adapter.setList(it)
                mViewDataBinding.projectHiddenHeader.visibility = View.GONE
                mViewDataBinding.projectsRV.visibility = View.VISIBLE
            } else {
                mViewDataBinding.projectHiddenHeader.visibility = View.VISIBLE
                mViewDataBinding.projectsRV.visibility = View.GONE
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            if (firstStartOffragment) {
                firstStartOffragment = false
                if (!sharedViewModel?.projectSearchQuery?.value?.trim().isNullOrEmpty()) {
                    sharedViewModel?.projectSearchQuery?.value?.let { viewModel.filterHiddenProjects(it) }
                }
            }
        }, 100)

        sharedViewModel?.projectSearchQuery?.observe(viewLifecycleOwner) {
            viewModel.filterHiddenProjects(it)
        }


    }

    private fun initRecyclerView(adapter: HiddenProjectAdapter) {
        mViewDataBinding.projectsRV.adapter = adapter
        adapter.setCallBack {
            when (it.first) {
                "root" -> {
                    CeibroApplication.CookiesManager.projectNameForDetails = it.second.title
                    CeibroApplication.CookiesManager.projectDataForDetails = it.second
//                    val bundle = Bundle()
//                    bundle.putParcelable("projectData", it.second)
                    navigate(R.id.projectDetailV2Fragment)
                }

                "unHide" -> {
                    showUnHideTaskDialog(requireContext(), it.second)
                }
            }

        }

    }

    private fun showUnHideTaskDialog(
        context: Context,
        projectData: CeibroProjectV2
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_custom_dialog, null)

        val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(view)
        val alertDialog = builder.create()

        val yesBtn = view.findViewById<Button>(R.id.yesBtn)
        val noBtn = view.findViewById<Button>(R.id.noBtn)
        val dialogText = view.findViewById<TextView>(R.id.dialog_text)
        dialogText.text = context.resources.getString(R.string.do_you_want_to_un_hide_this_project)
        alertDialog.window?.setBackgroundDrawable(null)
        alertDialog.show()

        yesBtn.setOnClickListener {
            alertDialog.dismiss()
            viewModel.unHideProject(!(projectData.isHiddenByMe), projectData._id) {

            }
        }

        noBtn.setOnClickListener {
            alertDialog.dismiss()
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshProjectsData(event: LocalEvents.RefreshProjectsData?) {
        loadProjects()
    }

    fun loadProjects() {
        viewModel.getHiddenProjects()
    }
}