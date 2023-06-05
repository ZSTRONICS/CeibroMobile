package com.zstronics.ceibro.ui.tasks.v2.newtask

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BackNavigationResult
import com.zstronics.ceibro.base.navgraph.BackNavigationResultListener
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.databinding.FragmentNewTaskV2Binding
import com.zstronics.ceibro.ui.pixiImagePicker.NavControllerSample
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.CeibroCameraActivity
import ee.zstronics.ceibro.camera.CeibroSmallImageRVAdapter
import ee.zstronics.ceibro.camera.PickedImages
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class NewTaskV2Fragment :
    BaseNavViewModelFragment<FragmentNewTaskV2Binding, INewTaskV2.State, NewTaskV2VM>(),
    BackNavigationResultListener {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: NewTaskV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_new_task_v2
    override fun toolBarVisibility(): Boolean = false
    private val TOPIC_REQUEST_CODE = 11
    private val ASSIGNEE_REQUEST_CODE = 12
    private val PROJECT_REQUEST_CODE = 13
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> navigateBack()
            R.id.newTaskTopicText -> navigateForResult(R.id.topicFragment, TOPIC_REQUEST_CODE)
            R.id.newTaskProjectText -> navigateForResult(
                R.id.taskProjectFragment,
                PROJECT_REQUEST_CODE
            )

            R.id.newTaskAssignToText -> {
                val bundle = Bundle()
                bundle.putParcelableArray(
                    "contacts",
                    viewState.selectedContacts.value?.toTypedArray()
                )
                navigateForResult(R.id.assigneeFragment, ASSIGNEE_REQUEST_CODE, bundle)
            }

            R.id.newTaskDueDateText -> {
                val datePicker =
                    DatePickerDialog(
                        requireContext(),
                        dueDateSetListener,
                        // set DatePickerDialog to point to today's date when it loads up
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    )
                datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
                datePicker.show()
            }

            R.id.newTaskTopicClearBtn -> {
                viewState.taskTitle.value = ""
                viewState.selectedTopic = MutableLiveData()
            }

            R.id.newTaskAssignToClearBtn -> {
                viewState.assignToText.value = ""
                viewState.selectedContacts = MutableLiveData()
            }

            R.id.newTaskProjectClearBtn -> {
                viewState.projectText.value = ""
                viewState.selectedProject = MutableLiveData()
            }

            R.id.newTaskDueDateClearBtn -> {
                viewState.dueDate.value = ""
            }

            R.id.newTaskPhotoBtn -> {
                val ceibroCamera = Intent(
                    requireContext(),
                    CeibroCameraActivity::class.java
                )
                ceibroImagesPickerLauncher.launch(ceibroCamera)
            }

            R.id.newTaskAttachBtn -> {
                if (viewState.isAttachLayoutOpen.value == true) {
                    viewState.isAttachLayoutOpen.value = false
                    mViewDataBinding.newTaskAttachmentLayout.animate()
                        .translationY(mViewDataBinding.newTaskAttachmentLayout.height.toFloat())
                        .setDuration(350)
                        .withEndAction {
                            mViewDataBinding.newTaskAttachmentLayout.visibility = View.GONE
                        }
                        .start()
                } else {
                    viewState.isAttachLayoutOpen.value = true
                    mViewDataBinding.newTaskAttachmentLayout.visibility = View.VISIBLE
                    mViewDataBinding.newTaskAttachmentLayout.animate()
                        .translationY(0f)
                        .setDuration(350)
                        .start()
                }
            }
        }
    }

    var smallImageAdapter: CeibroSmallImageRVAdapter = CeibroSmallImageRVAdapter()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewState.taskTitle.observe(viewLifecycleOwner) {
            if (it == "") {
                mViewDataBinding.newTaskTopicClearBtn.visibility = View.GONE
            } else {
                mViewDataBinding.newTaskTopicClearBtn.visibility = View.VISIBLE
            }
        }
        viewState.assignToText.observe(viewLifecycleOwner) {
            if (it == "") {
                mViewDataBinding.newTaskAssignToClearBtn.visibility = View.GONE
            } else {
                mViewDataBinding.newTaskAssignToClearBtn.visibility = View.VISIBLE
            }
        }
        viewState.projectText.observe(viewLifecycleOwner) {
            if (it == "") {
                mViewDataBinding.newTaskProjectClearBtn.visibility = View.GONE
            } else {
                mViewDataBinding.newTaskProjectClearBtn.visibility = View.VISIBLE
            }
        }
        viewState.dueDate.observe(viewLifecycleOwner) {
            if (it == "") {
                mViewDataBinding.newTaskDueDateClearBtn.visibility = View.GONE
            } else {
                mViewDataBinding.newTaskDueDateClearBtn.visibility = View.VISIBLE
            }
        }



        mViewDataBinding.newTaskDoneReqSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            viewState.isDoneReqAllowed.value = isChecked
        }
        val handler = Handler()
        handler.postDelayed(Runnable {
            mViewDataBinding.newTaskAttachmentLayout.animate()
                .translationY(mViewDataBinding.newTaskAttachmentLayout.height.toFloat())
                .setDuration(20)
                .withEndAction { mViewDataBinding.newTaskAttachmentLayout.visibility = View.GONE }
                .start()
        }, 20)

        mViewDataBinding.newTaskDescriptionText.setOnTouchListener { view, event ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                view.parent.requestDisallowInterceptTouchEvent(false)
            }
            return@setOnTouchListener false
        }

        viewModel.listOfImages.observe(viewLifecycleOwner) {
            smallImageAdapter.setList(it)
            mViewDataBinding.smallFooterImagesRV.visibility =
                if (it.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
        mViewDataBinding.smallFooterImagesRV.adapter = smallImageAdapter
    }

    var cal: Calendar = Calendar.getInstance()

    private val dueDateSetListener =
        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDueDateInView()
        }

    private fun updateDueDateInView() {
        val formatToSend = "dd.MM.yyyy"
        val sdf1 = SimpleDateFormat(formatToSend, Locale.US)

        viewState.dueDate.value = sdf1.format(cal.time)
    }

    private val ceibroImagesPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val images = result.data?.extras?.getParcelableArrayList<PickedImages>("images")
                if (images != null) {
                    val oldImages = viewModel.listOfImages.value
                    oldImages?.addAll(images)
                    viewModel.listOfImages.postValue(oldImages)
                }
            }
        }


    override fun onNavigationResult(result: BackNavigationResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            when (result.requestCode) {
                TOPIC_REQUEST_CODE -> {
                    val selectedTopic =
                        result.data?.getParcelable<TopicsResponse.TopicData>("topic")
                    if (selectedTopic != null) {
                        viewState.selectedTopic.value = selectedTopic
                        viewState.taskTitle.value = selectedTopic.topic
                    } else {
                        shortToastNow(resources.getString(R.string.topic_not_selected))
                    }
                }

                ASSIGNEE_REQUEST_CODE -> {
                    val selectedContact = result.data?.getParcelableArray("contacts")
                    val selectedContactList =
                        selectedContact?.map { it as AllCeibroConnections.CeibroConnection }
                            ?.toMutableList()
                    var assigneeMembers = ""

                    var index = 0
                    if (selectedContactList != null) {
                        for (item in selectedContactList) {
                            assigneeMembers += if (index == selectedContactList.size - 1) {
                                "${item.contactFullName}"
                            } else {
                                "${item.contactFullName}; "
                            }
                            index++
                        }
                        viewState.assignToText.value = assigneeMembers
                        viewState.selectedContacts.value = selectedContactList
                    }
                }

                PROJECT_REQUEST_CODE -> {
                    val selectedProject =
                        result.data?.getParcelable<AllProjectsResponse.Projects>("project")
                    if (selectedProject != null) {
                        viewState.selectedProject.value = selectedProject
                        viewState.projectText.value = selectedProject.title
                    } else {
                        shortToastNow(resources.getString(R.string.project_not_selected))
                    }
                }
            }
        }
    }

}