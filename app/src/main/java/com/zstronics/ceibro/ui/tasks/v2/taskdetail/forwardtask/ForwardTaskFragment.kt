package com.zstronics.ceibro.ui.tasks.v2.taskdetail.forwardtask

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.extensions.showKeyboardWithFocus
import com.zstronics.ceibro.base.navgraph.BackNavigationResult
import com.zstronics.ceibro.base.navgraph.BackNavigationResultListener
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.task.models.v2.ForwardTaskV2Request
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.databinding.FragmentCommentBinding
import com.zstronics.ceibro.databinding.FragmentForwardTaskBinding
import com.zstronics.ceibro.extensions.openFilePicker
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.CeibroFilesRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.CeibroImageWithCommentRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.CeibroOnlyImageRVAdapter
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.CeibroCameraActivity
import ee.zstronics.ceibro.camera.FileUtils
import ee.zstronics.ceibro.camera.PickedImages
import javax.inject.Inject

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
                viewModel.forwardTask() { task ->
                    val bundle = Bundle()
                    bundle.putParcelable("taskData", task)
                    navigateBackWithResult(Activity.RESULT_OK, bundle)
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