package com.zstronics.ceibro.ui.tasks.v2.newtask

import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BackNavigationResult
import com.zstronics.ceibro.base.navgraph.BackNavigationResultListener
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponseV2
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.databinding.FragmentNewTaskV2Binding
import com.zstronics.ceibro.extensions.openFilePicker
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.CeibroFilesRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.CeibroImageWithCommentRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.CeibroOnlyImageRVAdapter
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.CeibroCameraActivity
import ee.zstronics.ceibro.camera.FileUtils
import ee.zstronics.ceibro.camera.PickedImages
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


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
    private val PICK_FILE_REQUEST = 1
    private var isExpanded = true
    private val expandDuration = 100L

    override fun onClick(id: Int) {
        when (id) {
            R.id.nextBtn -> {
                var doneImageRequired = false
                var doneCommentsRequired = false
                if (mViewDataBinding.newTaskDoneReqSwitch.isChecked) {
                    doneImageRequired = mViewDataBinding.imageCheckbox.isChecked
                    doneCommentsRequired = mViewDataBinding.commentCheckbox.isChecked
                }
                viewModel.createNewTask(
                    requireContext(),
                    doneImageRequired = doneImageRequired,
                    doneCommentsRequired = doneCommentsRequired
                ) {
                    val bundle = Bundle()
                    bundle.putBoolean("createdNewTask", true)
                    navigateBackWithResult(RESULT_OK, bundle)
                }
            }
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
                bundle.putBoolean("self-assign", viewState.selfAssigned.value ?: false)
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
                viewState.selfAssigned.value = false
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

            R.id.newTaskDocumentBtn -> {
                chooseDocuments(
                    mimeTypes = arrayOf(
                        "text/plain",
                        "text/csv",
                        "application/pdf",
                        "application/rtf",
                        "application/zip",
                        "application/vnd.oasis.opendocument.text",                                  // .odt
                        "application/vnd.oasis.opendocument.spreadsheet",                           // .ods
                        "application/vnd.oasis.opendocument.presentation",                          // .odp
                        "application/x-rar-compressed",
                        "application/vnd.android.package-archive",      //for APK file
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",  // .docx
                        "application/vnd.ms-word.document.macroEnabled.12",                         // .doc
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",        // .xlsx
                        "application/vnd.ms-excel.sheet.macroEnabled.12",                           // .xls
                        "application/vnd.ms-powerpoint",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
                        "application/vnd.ms-powerpoint.presentation.macroEnabled.12"                 // .ppt
//                        "image/vnd.adobe.photoshop", // Photoshop Document (PSD)
//                        "image/vnd.dwg" // AutoCAD Drawing Database (DWG)
                    )
                )
            }

            R.id.newTaskLibraryBtn -> {
                chooseImages(
                    mimeTypes = arrayOf(
                        "image/jpeg",
                        "image/jpg",
                        "image/png",
                        "image/gif",
                        "image/webp",
                        "image/bmp",
                        "image/x-icon",
                        "image/svg+xml",
                        "image/tiff",
                        "image/*"
                    )
                )
            }

            R.id.filesLayout -> toggleLayout(mViewDataBinding.filesLayout)
        }
    }


    @Inject
    lateinit var onlyImageAdapter: CeibroOnlyImageRVAdapter

    @Inject
    lateinit var imageWithCommentAdapter: CeibroImageWithCommentRVAdapter

    @Inject
    lateinit var filesAdapter: CeibroFilesRVAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.onlyImagesRV.isNestedScrollingEnabled = false
        mViewDataBinding.imagesWithCommentRV.isNestedScrollingEnabled = false
        mViewDataBinding.filesRV.isNestedScrollingEnabled = false

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
            if (!it.isNullOrEmpty()) {
                val allImages = it

                val onlyImages1 = arrayListOf<PickedImages>()
                val imagesWithComment1 = arrayListOf<PickedImages>()
                for (item in allImages) {
                    if (item.comment.isNotEmpty()) {
                        imagesWithComment1.add(item)
                    } else {
                        onlyImages1.add(item)
                    }
                }
                viewModel.onlyImages.postValue(onlyImages1)
                viewModel.imagesWithComments.postValue(imagesWithComment1)
            }
        }

        viewModel.imagesWithComments.observe(viewLifecycleOwner) {
            imageWithCommentAdapter.setList(it)
            if (it.isNotEmpty()) {
                mViewDataBinding.imagesWithCommentRV.visibility = View.VISIBLE
                mViewDataBinding.imagesWithCommentBottomLine.visibility = View.VISIBLE
            } else {
                mViewDataBinding.imagesWithCommentRV.visibility = View.GONE
                mViewDataBinding.imagesWithCommentBottomLine.visibility = View.GONE
            }
        }
        mViewDataBinding.imagesWithCommentRV.adapter = imageWithCommentAdapter


        viewModel.onlyImages.observe(viewLifecycleOwner) {
            onlyImageAdapter.setList(it)
            if (it.isNotEmpty()) {
                mViewDataBinding.onlyImagesRV.visibility = View.VISIBLE
                mViewDataBinding.onlyImagesBottomLine.visibility = View.VISIBLE
            } else {
                mViewDataBinding.onlyImagesRV.visibility = View.GONE
                mViewDataBinding.onlyImagesBottomLine.visibility = View.GONE
            }
        }
        mViewDataBinding.onlyImagesRV.adapter = onlyImageAdapter


        viewModel.documents.observe(viewLifecycleOwner) {
            filesAdapter.setList(it)
            if (it.isNotEmpty()) {
                mViewDataBinding.filesLayout.visibility = View.VISIBLE
                mViewDataBinding.filesBottomLine.visibility = View.VISIBLE
            } else {
                mViewDataBinding.filesLayout.visibility = View.GONE
                mViewDataBinding.filesBottomLine.visibility = View.GONE
            }
            mViewDataBinding.filesCount.text = "${it.size} file(s)"
        }
        mViewDataBinding.filesRV.adapter = filesAdapter

        filesAdapter.itemClickListener = { _: View, position: Int, data: PickedImages ->
            val oldDocuments = viewModel.documents.value
            oldDocuments?.remove(data)
            viewModel.documents.postValue(oldDocuments)
        }
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


    private fun chooseImages(mimeTypes: Array<String>) {
        requireActivity().openFilePicker(
            mimeTypes = mimeTypes,
            allowMultiple = true
        ) { resultCode, data ->
            val pickedImage = arrayListOf<PickedImages>()
            val oldImages = viewModel.onlyImages.value

            if (resultCode == RESULT_OK && data != null) {
                val clipData = data.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val fileUri = clipData.getItemAt(i).uri
                        val selectedImgDetail = getPickedFileDetail(requireContext(), fileUri)

                        if (oldImages?.contains(selectedImgDetail) == true) {
                            shortToastNow("You selected an already-added image")
                        } else {
                            pickedImage.add(selectedImgDetail)
                        }
                    }
                } else {
                    val fileUri = data.data
                    fileUri.let {
                        val selectedImgDetail = getPickedFileDetail(requireContext(), it)

                        if (oldImages?.contains(selectedImgDetail) == true) {
                            shortToastNow("You selected an already-added image")
                        } else {
                            pickedImage.add(selectedImgDetail)
                        }
                    }
                }
            }

            val allOldImages = viewModel.listOfImages.value
            allOldImages?.addAll(pickedImage)
            viewModel.listOfImages.postValue(allOldImages)
        }
    }

    private fun chooseDocuments(mimeTypes: Array<String>) {
        requireActivity().openFilePicker(
            mimeTypes = mimeTypes,
            allowMultiple = true
        ) { resultCode, data ->
            val pickedDocuments = arrayListOf<PickedImages>()
            val oldDocuments = viewModel.documents.value

            if (resultCode == RESULT_OK && data != null) {
                val clipData = data.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val fileUri = clipData.getItemAt(i).uri
                        val selectedDocDetail = getPickedFileDetail(requireContext(), fileUri)

                        if (oldDocuments?.contains(selectedDocDetail) == true) {
                            shortToastNow("You selected an already-added document")
                        } else {
                            pickedDocuments.add(selectedDocDetail)
                        }
                    }
                } else {
                    val fileUri = data.data
                    fileUri.let {
                        val selectedDocDetail = getPickedFileDetail(requireContext(), it)

                        if (oldDocuments?.contains(selectedDocDetail) == true) {
                            shortToastNow("You selected an already-added document")
                        } else {
                            pickedDocuments.add(selectedDocDetail)
                        }
                    }
                }
            }

            oldDocuments?.addAll(pickedDocuments)
            viewModel.documents.postValue(oldDocuments)
        }
    }

    private fun getPickedFileDetail(context: Context, fileUri: Uri?): PickedImages {
        val mimeType = FileUtils.getMimeType(context, fileUri)
        val fileName = FileUtils.getFileName(context, fileUri)
        val fileSize = FileUtils.getFileSizeInBytes(context, fileUri)
        val fileSizeReadAble = FileUtils.getReadableFileSize(fileSize)
        val attachmentType = when {
            mimeType == "application/pdf" -> {
                AttachmentTypes.Pdf
            }

            mimeType.equals("text/plain", true) ||
                    mimeType.equals("text/csv", true) ||
                    mimeType.equals("application/rtf", true) ||
                    mimeType.equals("application/zip", true) ||
                    mimeType.equals("application/x-rar-compressed", true) ||
                    mimeType.equals("application/vnd.oasis.opendocument.text", true) ||
                    mimeType.equals("application/vnd.oasis.opendocument.spreadsheet", true) ||
                    mimeType.equals("application/vnd.oasis.opendocument.presentation", true) ||
                    mimeType.equals("application/vnd.android.package-archive", true) ||
                    mimeType.equals("application/msword", true) ||
                    mimeType.equals(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        true
                    ) ||
                    mimeType.equals(
                        "application/vnd.ms-word.document.macroEnabled.12",
                        true
                    ) ||
                    mimeType.equals("application/vnd.ms-excel", true) ||
                    mimeType.equals(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        true
                    ) ||
                    mimeType.equals(
                        "application/vnd.ms-excel.sheet.macroEnabled.12",
                        true
                    ) ||
                    mimeType.equals("application/vnd.ms-powerpoint", true) ||
                    mimeType.equals(
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        true
                    ) ||
                    mimeType.equals(
                        "application/vnd.ms-powerpoint.presentation.macroEnabled.12",
                        true
                    ) -> {
                AttachmentTypes.Doc
            }

            mimeType.startsWith("image") -> {
                AttachmentTypes.Image
            }

            mimeType.startsWith("video") -> {
                AttachmentTypes.Video
            }

            else -> AttachmentTypes.Doc
        }
        return PickedImages(
            fileUri = fileUri,
            attachmentType = attachmentType,
            fileName = fileName,
            fileSizeReadAble = fileSizeReadAble,
            file = FileUtils.getFile(requireContext(), fileUri)
        )
    }


    override fun onNavigationResult(result: BackNavigationResult) {
        if (result.resultCode == RESULT_OK) {
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
                    val selfAssigned = result.data?.getBoolean("self-assign")
                    val selectedContact = result.data?.getParcelableArray("contacts")
                    val selectedContactList =
                        selectedContact?.map { it as AllCeibroConnections.CeibroConnection }
                            ?.toMutableList()
                    val selectedItem = selectedContactList?.find { item1 ->
                        item1.id == viewModel.user?.id
                    }
                    if (selectedItem != null) {
                        val index = selectedContactList.indexOf(selectedItem)
                        selectedContactList.removeAt(index)
                    }

                    var assigneeMembers = ""

                    if (selfAssigned != null) {
                        if (selfAssigned) {
                            assigneeMembers += if (selectedContactList.isNullOrEmpty()) {
                                "Me"
                            } else {
                                "Me; "
                            }
                        }
                        viewState.selfAssigned.value = selfAssigned
                    }

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
                        viewState.selectedContacts.value = selectedContactList
                    }
                    viewState.assignToText.value = assigneeMembers
                }

                PROJECT_REQUEST_CODE -> {
                    val selectedProject =
                        result.data?.getParcelable<AllProjectsResponseV2.ProjectsV2>("project")
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


    private fun toggleLayout(filesLayout: ConstraintLayout) {
        if (isExpanded) {
            collapseLayout(filesLayout)
        } else {
            expandLayout(filesLayout)
        }
        isExpanded = !isExpanded
    }

    private fun expandLayout(fileLayout: ConstraintLayout) {
        mViewDataBinding.downUpIcon.setImageResource(R.drawable.icon_navigate_up)
//        TransitionManager.beginDelayedTransition(fileLayout)
//        val transition = AutoTransition()
//        transition.duration = expandDuration

        val params = fileLayout.layoutParams
        params.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
        fileLayout.layoutParams = params
    }

    private fun collapseLayout(fileLayout: ConstraintLayout) {
        mViewDataBinding.downUpIcon.setImageResource(R.drawable.icon_navigate_down)
//        val transition = AutoTransition()
//        transition.duration = expandDuration
//        TransitionManager.beginDelayedTransition(fileLayout, transition)

        val params = fileLayout.layoutParams
        params.height = 91
        fileLayout.layoutParams = params
    }

}