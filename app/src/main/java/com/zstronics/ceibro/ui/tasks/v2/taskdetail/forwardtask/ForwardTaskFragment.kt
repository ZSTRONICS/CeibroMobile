package com.zstronics.ceibro.ui.tasks.v2.taskdetail.forwardtask

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.finish
import com.zstronics.ceibro.base.extensions.launchActivityWithFinishAffinity
import com.zstronics.ceibro.base.extensions.showKeyboardWithFocus
import com.zstronics.ceibro.base.navgraph.BackNavigationResult
import com.zstronics.ceibro.base.navgraph.BackNavigationResultListener
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.databinding.FragmentForwardTaskBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForwardTaskFragment :
    BaseNavViewModelFragment<FragmentForwardTaskBinding, IForwardTask.State, ForwardTaskVM>(),
    BackNavigationResultListener {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ForwardTaskVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_forward_task
    override fun toolBarVisibility(): Boolean = false
    val FORWARD_REQUEST_CODE = 105
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> {
                CeibroApplication.CookiesManager.taskIdInDetails = ""
                val instances = countActivitiesInBackStack(requireContext())
                if (viewModel.notificationTaskData.value != null) {
                    if (instances <= 1) {
                        launchActivityWithFinishAffinity<NavHostPresenterActivity>(
                            options = Bundle(),
                            clearPrevious = true
                        ) {
                            putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
                            putExtra(
                                NAVIGATION_Graph_START_DESTINATION_ID,
                                R.id.homeFragment
                            )
                        }
                    } else {
                        //finish is called so that second instance of app will be closed and only one last instance will remain
                        finish()
                    }
                } else {
                    navigateBack()
                }
            }
            R.id.forwardToText -> {
                val bundle = Bundle()
                bundle.putStringArrayList(
                    "assignToContacts",
                    viewModel.oldSelectedContacts
                )
                bundle.putParcelableArray(
                    "selectedContacts",
                    viewModel.selectedContacts.value?.toTypedArray()
                )
                navigateForResult(R.id.forwardFragment, FORWARD_REQUEST_CODE, bundle)
            }

            R.id.forwardToClearBtn -> {
                viewState.forwardToText.value = ""
                viewModel.selectedContacts = MutableLiveData()
            }

            R.id.forwardBtn -> {
                viewModel.forwardTask { eventData ->
                    CeibroApplication.CookiesManager.taskIdInDetails = ""
                    if (viewModel.notificationTaskData.value != null) {
                        val instances = countActivitiesInBackStack(requireContext())
                        if (instances <= 1) {
                            launchActivityWithFinishAffinity<NavHostPresenterActivity>(
                                options = Bundle(),
                                clearPrevious = true
                            ) {
                                putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
                                putExtra(
                                    NAVIGATION_Graph_START_DESTINATION_ID,
                                    R.id.homeFragment
                                )
                            }
                        } else {
                            //finish is called so that second instance of app will be closed and only one last instance will remain
                            finish()
                        }
                    } else {
                        val bundle = Bundle()
                        bundle.putParcelable("eventData", eventData)
                        navigateBackWithResult(Activity.RESULT_OK, bundle)
                    }
                }
            }
        }
    }

    //This function is called when fragment is closed and detach from activity
    override fun onDetach() {
        CeibroApplication.CookiesManager.taskIdInDetails = ""
        super.onDetach()
    }

    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            CeibroApplication.CookiesManager.taskIdInDetails = ""
            val instances = countActivitiesInBackStack(requireContext())
            if (instances <= 1) {
                launchActivityWithFinishAffinity<NavHostPresenterActivity>(
                    options = Bundle(),
                    clearPrevious = true
                ) {
                    putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
                    putExtra(
                        NAVIGATION_Graph_START_DESTINATION_ID,
                        R.id.homeFragment
                    )
                }
            } else {
                //finish is called so that second instance of app will be closed and only one last instance will remain
                finish()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.notificationTaskData.observe(viewLifecycleOwner) { notificationData ->
            if (notificationData != null) {
                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
            }
        }

        mViewDataBinding.commentText.setOnTouchListener { view, event ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                view.parent.requestDisallowInterceptTouchEvent(false)
            }
            return@setOnTouchListener false
        }


        viewState.forwardToText.observe(viewLifecycleOwner) {
            if (it == "") {
                mViewDataBinding.forwardToClearBtn.visibility = View.GONE
            } else {
                mViewDataBinding.forwardToClearBtn.visibility = View.VISIBLE
            }
        }

        val handler1 = Handler()
        handler1.postDelayed(Runnable {
            mViewDataBinding.commentText.post {
                mViewDataBinding.commentText.showKeyboardWithFocus()
            }
        }, 300)

    }


    override fun onNavigationResult(result: BackNavigationResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            when (result.requestCode) {
                FORWARD_REQUEST_CODE -> {
                    val selectedContact = result.data?.getParcelableArray("forwardContacts")
                    val selectedContactList =
                        selectedContact?.map { it as AllCeibroConnections.CeibroConnection }
                            ?.toMutableList()

                    var assigneeMembers = ""
                    var index = 0
                    if (selectedContactList != null) {
                        for (item in selectedContactList) {
                            assigneeMembers += if (index == selectedContactList.size - 1) {
                                "${item.contactFirstName} ${item.contactSurName}"
                            } else {
                                "${item.contactFirstName} ${item.contactSurName}; "
                            }
                            index++
                        }
                        viewModel.selectedContacts.value = selectedContactList
                    }
                    viewState.forwardToText.value = assigneeMembers

                }

            }
        }
    }

    private fun countActivitiesInBackStack(context: Context): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTasks = activityManager.appTasks
        var activityCount = 0

        for (task in runningTasks) {
            val taskInfo = task.taskInfo
            activityCount += taskInfo.numActivities
        }

        return activityCount
    }
}