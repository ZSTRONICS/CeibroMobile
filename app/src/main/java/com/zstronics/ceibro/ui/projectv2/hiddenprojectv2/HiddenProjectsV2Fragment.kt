package com.zstronics.ceibro.ui.projectv2.hiddenprojectv2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.databinding.FragmentHiddenProjectsV2Binding
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject


@AndroidEntryPoint
class HiddenProjectsV2Fragment(callback: (Int) -> Unit) :
    BaseNavViewModelFragment<FragmentHiddenProjectsV2Binding, IHiddenProjectV2.State, HiddenProjectsV2VM>() {

    var callback: ((Int) -> Unit)? = null
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: HiddenProjectsV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_hidden_projects_v2
    override fun toolBarVisibility(): Boolean = false

    init {
        this.callback = callback
    }

    @Inject
    lateinit var adapter: HiddenProjectAdapter

    override fun onClick(id: Int) {

        when (id) {

            R.id.cl_AddNewProject -> {
                navigate(R.id.newProjectV2Fragment)
            }

            R.id.tvNewProject -> {
                navigate(R.id.newProjectV2Fragment)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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


    }

    private fun initRecyclerView(adapter: HiddenProjectAdapter) {
        mViewDataBinding.projectsRV.adapter = adapter
        adapter.setCallBack {
            when (it.first) {
                "root" -> {
                    CookiesManager.projectNameForDetails = it.second.title
                    CookiesManager.projectDataForDetails = it.second
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
                callback?.invoke(1)
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