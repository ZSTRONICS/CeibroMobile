package com.zstronics.ceibro.ui.tasks.v2.taskdetail.forwardtask

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.launchActivityWithFinishAffinity
import com.zstronics.ceibro.base.extensions.shortToastNow
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
            R.id.backBtn -> navigateBack()
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
                viewModel.forwardTask { task ->

                    if (viewModel.taskData2 != null) {

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
                        shortToastNow("Task forwarded successfully!")

                    } else {
                        val bundle = Bundle()
                        bundle.putParcelable("taskData", task)
                        navigateBackWithResult(Activity.RESULT_OK, bundle)
                    }
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.commentText.setOnTouchListener { view, event ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                view.parent.requestDisallowInterceptTouchEvent(false)
            }
            return@setOnTouchListener false
        }

        val notificationManager = NotificationManagerCompat.from(requireContext())

        viewModel.notificationId.observe(viewLifecycleOwner) {
            notificationManager.cancel(it)
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
}