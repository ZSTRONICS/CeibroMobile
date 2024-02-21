package com.zstronics.ceibro.ui.tasks.v2.newtask

import android.app.Activity.RESULT_OK
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import com.zstronics.ceibro.base.extensions.longToastNow
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BackNavigationResult
import com.zstronics.ceibro.base.navgraph.BackNavigationResultListener
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.viewmodel.Dispatcher
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.databinding.FragmentNewTaskV2Binding
import com.zstronics.ceibro.extensions.openFilePicker
import com.zstronics.ceibro.ui.locationv2.usage.AddLocationTask
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.CeibroFilesRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.CeibroImageWithCommentRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.CeibroOnlyImageRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.TagsChipsAdapter
import com.zstronics.ceibro.ui.tasks.v2.newtask.assignee.adapter.AssigneeChipsAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.comment.EditCommentDialogSheet
import com.zstronics.ceibro.utils.DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.CeibroCameraActivity
import ee.zstronics.ceibro.camera.CeibroImageViewerActivity
import ee.zstronics.ceibro.camera.FileUtils
import ee.zstronics.ceibro.camera.PickedImages
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
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
    private val DRAWING_REQUEST_CODE = 14
    private val CONFIRMER_REQUEST_CODE = 15
    private val VIEWER_REQUEST_CODE = 16
    private val TAG_REQUEST_CODE = 17
    private val PICK_FILE_REQUEST = 1
    private var isExpanded = true
    private val expandDuration = 100L

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalZeroShutterLag::class)
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
                    doneImageRequired = doneImageRequired,
                    doneCommentsRequired = doneCommentsRequired,
                    requireActivity()
                ) {
                    if (it.equals("ServiceCall", true)) {
                        context?.let { context ->
                            showToast(context.getString(R.string.creating_task_with_files))
                            val serviceIntent = Intent(context, CreateNewTaskService::class.java)
                            serviceIntent.putExtra("ServiceRequest", "taskRequest")
                            context.startService(serviceIntent)
                            navigateBack()
                        }
                    } else if (it.equals("taskCreatedLocally", true)) {
                        navigateBack()
                    } else {
                        val bundle = Bundle()
                        bundle.putBoolean("createdNewTask", true)
                        navigateBackWithResult(RESULT_OK, bundle)
                    }
                }
            }

            R.id.backBtn -> navigateBack()
            R.id.newTaskTopicText -> navigateForResult(R.id.topicFragment, TOPIC_REQUEST_CODE)
            R.id.newTaskProjectText -> navigateForResult(
                R.id.taskProjectFragment,
                PROJECT_REQUEST_CODE
            )


            R.id.newConfirmerTopicText -> {
                viewState.selectedConfirmerContacts.value?.clear()
                val bundle = Bundle()
                bundle.putParcelableArray(
                    "contacts",
                    viewState.selectedConfirmerContacts.value?.toTypedArray()
                )
                bundle.putBoolean("self-assign", viewState.selfAssignedConfermer.value ?: false)
                bundle.putBoolean("isConfirmer", true)
                bundle.putBoolean("isViewer", false)
                navigateForResult(R.id.assigneeFragment, CONFIRMER_REQUEST_CODE, bundle)
            }

            R.id.newViewerTopicText -> {
                val bundle = Bundle()
                bundle.putParcelableArray(
                    "contacts",
                    viewState.selectedViewerContacts.value?.toTypedArray()
                )
                bundle.putBoolean("self-assign", viewState.selfAssignedViewer.value ?: false)
                bundle.putBoolean("isConfirmer", false)
                bundle.putBoolean("isViewer", true)
                navigateForResult(R.id.assigneeFragment, VIEWER_REQUEST_CODE, bundle)
            }

            R.id.newTagTopicText -> {
                navigateForResult(R.id.tagsFragment, TAG_REQUEST_CODE)
            }


            R.id.newTaskAssignToText -> {
                val bundle = Bundle()
                bundle.putParcelableArray(
                    "contacts",
                    viewState.selectedContacts.value?.toTypedArray()
                )
                bundle.putBoolean("self-assign", viewState.selfAssigned.value ?: false)
                bundle.putBoolean("isConfirmer", false)
                bundle.putBoolean("isViewer", false)
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

            R.id.confirmerEndLayoutClearBtn -> {
                viewState.selfAssignedConfermer.value = false
                viewState.confirmerText.value = ""
                viewState.selectedConfirmerContacts = MutableLiveData()
            }

            R.id.viewerEndLayoutClearBtn -> {
                viewState.selfAssignedViewer.value = false
                viewState.viewerText.value = ""
                viewState.selectedViewerContacts = MutableLiveData()
            }

            R.id.tagEndLayoutClearBtn -> {
                viewState.tagText.value = ""
            }

            R.id.newTaskAssignToClearBtn -> {
                viewState.assignToText.value = ""
                viewState.selfAssigned.value = false
                viewState.selectedContacts = MutableLiveData()
            }

            R.id.newTaskProjectClearBtn -> {
                viewState.projectText.value = ""
                viewState.selectedProject = MutableLiveData()
                mViewDataBinding.newTaskLocationBtn.isEnabled = false
                mViewDataBinding.newTaskLocationBtn.isClickable = false
            }

            R.id.newTaskDueDateClearBtn -> {
                viewState.dueDate.value = ""
            }

            R.id.newTaskLocationBtn -> {
                openAddLocationSheet()
                if (viewModel.locationTaskData.value != null) {
                    shortToastNow("Existing location will be removed, if new pin is marked")
                }
            }

            R.id.newTaskPhotoBtn -> {
                val listOfPickedImages = arrayListOf<PickedImages>()
                viewModel.listOfImages.value?.let { listOfPickedImages.addAll(it) }
                val bundle = Bundle()
                bundle.putParcelableArrayList("allImagesList", listOfPickedImages)
                val ceibroCamera = Intent(
                    requireContext(),
                    CeibroCameraActivity::class.java
                )
                ceibroCamera.putExtra("allImagesBundle", bundle)
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
//                        "image/vnd.dwg",    // AutoCAD Drawing Database (DWG)
//                        "application/acad"  // AutoCAD Drawing
//                        "image/vnd.adobe.photoshop", // Photoshop Document (PSD)
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
                        "image/*"
//                        "image/x-icon",
//                        "image/svg+xml",
//                        "image/tiff"
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


    @Inject
    lateinit var chipAdapter: TagsChipsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        mViewDataBinding.selectedTagsRV.adapter = chipAdapter

        mViewDataBinding.newTaskParentScroll.isSmoothScrollingEnabled = true
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
        viewState.confirmerText.observe(viewLifecycleOwner) {
            if (it == "") {
                mViewDataBinding.confirmerEndLayoutClearBtn.visibility = View.GONE
            } else {
                mViewDataBinding.confirmerEndLayoutClearBtn.visibility = View.VISIBLE
            }
        }

        viewState.viewerText.observe(viewLifecycleOwner) {
            if (it == "") {
                mViewDataBinding.viewerEndLayoutClearBtn.visibility = View.GONE
            } else {
                mViewDataBinding.viewerEndLayoutClearBtn.visibility = View.VISIBLE
            }
        }

        viewState.tagText.observe(viewLifecycleOwner) {
            if (it == "") {
                mViewDataBinding.selectedTagsRV.visibility = View.GONE
            } else {
                mViewDataBinding.selectedTagsRV.visibility = View.VISIBLE
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
            if (viewModel.locationTaskData.value != null) {
                mViewDataBinding.newTaskProjectClearBtn.visibility = View.GONE
                mViewDataBinding.newTaskProjectText.isClickable = false
                if (!viewState.projectCannotChangeToastShown) {
                    viewState.projectCannotChangeToastShown = true
                    longToastNow("Project cannot be changed now, because location pin is added")
                }
            }
        }
        viewState.selectedProject.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.getGroupsByProjectID(it._id)
                mViewDataBinding.newTaskLocationBtn.isEnabled = true
                mViewDataBinding.newTaskLocationBtn.isClickable = true
            } else {
                mViewDataBinding.newTaskLocationBtn.isEnabled = false
                mViewDataBinding.newTaskLocationBtn.isClickable = false
            }
        }
        viewModel.originalGroups.observe(viewLifecycleOwner) {
//            if (!it.isNullOrEmpty()) {
//                viewModel.getGroupsByProjectID(it._id)
//                mViewDataBinding.newTaskLocationBtn.isEnabled = true
//                mViewDataBinding.newTaskLocationBtn.isClickable = true
//            } else {
//                mViewDataBinding.newTaskLocationBtn.isEnabled = false
//                mViewDataBinding.newTaskLocationBtn.isClickable = false
//            }
        }
        viewState.dueDate.observe(viewLifecycleOwner) {
            if (it == "") {
                mViewDataBinding.newTaskDueDateClearBtn.visibility = View.GONE
            } else {
                mViewDataBinding.newTaskDueDateClearBtn.visibility = View.VISIBLE
            }
        }
        viewModel.locationTaskData.observe(viewLifecycleOwner) {
            if (it != null) {
                val fileUri = Uri.fromFile(it.locationImgFile)
                val fileSize = FileUtils.getFileSizeInBytes(context, fileUri)
                val fileSizeReadAble = FileUtils.getReadableFileSize(fileSize)

                val locationImgData = PickedImages(
                    fileUri = fileUri,
                    attachmentType = AttachmentTypes.Drawing,
                    fileName = it.drawingName,
                    fileSizeReadAble = fileSizeReadAble,
                    locationImage = true,
                    file = it.locationImgFile
                )
                val allOldImages = viewModel.listOfImages.value
                val foundDrawingImg =
                    allOldImages?.find { oldImage -> oldImage.attachmentType == AttachmentTypes.Drawing }
                if (foundDrawingImg != null) {
                    val index = allOldImages.indexOf(foundDrawingImg)
                    allOldImages.removeAt(index)
                }

                allOldImages?.add(locationImgData)
                viewModel.listOfImages.postValue(allOldImages)

                mViewDataBinding.locationLayout.visibility = View.VISIBLE

                mViewDataBinding.newTaskProjectClearBtn.visibility = View.GONE
                mViewDataBinding.newTaskProjectText.isClickable = false
                mViewDataBinding.newTaskProjectText.isEnabled = false
                mViewDataBinding.newTaskProjectField.alpha = 0.6f

                mViewDataBinding.drawingName.text = it.drawingName

                val file = it.locationImgFile;
                if (file.exists()) {
                    val bitmap = getBitmapFromFile(file.absolutePath)
                    mViewDataBinding.drawingImg.setImageBitmap(bitmap)
                }

            } else {
                mViewDataBinding.locationLayout.visibility = View.GONE
            }
        }



        mViewDataBinding.newTaskDoneReqSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            viewState.isDoneReqAllowed.value = isChecked
            if (isChecked) {
                val handler = Handler()
                handler.postDelayed(Runnable {
                    mViewDataBinding.newTaskParentScroll.fullScroll(View.FOCUS_DOWN)
                }, 40)
            }
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
                    if (item.attachmentType == AttachmentTypes.Drawing) {
                        //do nothing because file is already displayed in location image section
                    } else if (item.comment.isNotEmpty()) {
                        imagesWithComment1.add(item)
                    } else {
                        onlyImages1.add(item)
                    }
                }
                viewModel.onlyImages.postValue(onlyImages1)
                viewModel.imagesWithComments.postValue(imagesWithComment1)
            } else {
                viewModel.onlyImages.postValue(arrayListOf())
                viewModel.imagesWithComments.postValue(arrayListOf())
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
        imageWithCommentAdapter.textClickListener = { _: View, position: Int, data: PickedImages ->
            showEditCommentDialog(data)
        }
        imageWithCommentAdapter.openImageClickListener =
            { _: View, position: Int, data: PickedImages ->
                /*    val bundle = Bundle()
                    bundle.putParcelableArray(
                        "images",
                        viewModel.imagesWithComments.value?.toTypedArray()
                    )
                    bundle.putInt("position", position)
                    bundle.putBoolean("fromServerUrl", false)
                    navigate(R.id.imageViewerFragment, bundle)*/

                val newList: ArrayList<PickedImages> = arrayListOf()
                //  val listOfPickedImages = result.data?.extras?.getParcelableArrayList<PickedImages>("images")
                val ceibroCamera = Intent(requireActivity(), CeibroImageViewerActivity::class.java)
                val bundle = Bundle()
                if (viewModel.listOfImages.value != null) {
                    newList.addAll(viewModel.listOfImages.value!!)
                }
                //  newList.addAll(oldImages)

                bundle.putParcelableArrayList("images", newList)
                bundle.putParcelable("object", data)
                bundle.putBoolean("isFromNewTaskFragment", true)
                ceibroCamera.putExtras(bundle)
                ceibroImageViewerLauncher.launch(ceibroCamera)

            }

        imageWithCommentAdapter.removeItemClickListener =
            {
                val listOfImages = viewModel.listOfImages.value
                if (listOfImages?.contains(it) == true) {
                    listOfImages.remove(it)
                    viewModel.listOfImages.postValue(listOfImages)
                }
            }


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
        onlyImageAdapter.openImageClickListener =
            { _: View, position: Int, fileUri: String, obj ->
                /*  val bundle = Bundle()
                  bundle.putParcelableArray("images", viewModel.onlyImages.value?.toTypedArray())
                  bundle.putInt("position", position)
                  bundle.putBoolean("fromServerUrl", false)
                  navigate(R.id.imageViewerFragment, bundle)
  */
                val newList: ArrayList<PickedImages> = arrayListOf()
                //  val listOfPickedImages = result.data?.extras?.getParcelableArrayList<PickedImages>("images")
                val ceibroCamera = Intent(requireActivity(), CeibroImageViewerActivity::class.java)
                val bundle = Bundle()
                if (viewModel.listOfImages.value != null) {
                    newList.addAll(viewModel.listOfImages.value!!)
                }
                //  newList.addAll(oldImages)

                bundle.putParcelableArrayList("images", newList)
                bundle.putParcelable("object", obj)
                bundle.putBoolean("isFromNewTaskFragment", true)
                ceibroCamera.putExtras(bundle)
                ceibroImageViewerLauncher.launch(ceibroCamera)

            }
        onlyImageAdapter.removeItemClickListener =
            {
                val listOfImages = viewModel.listOfImages.value
                if (listOfImages?.contains(it) == true) {
                    listOfImages.remove(it)
                    viewModel.listOfImages.postValue(listOfImages)
                }
            }

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
        val formatToSend = FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT
        val sdf1 = SimpleDateFormat(formatToSend, Locale.US)

        viewState.dueDate.value = sdf1.format(cal.time)
    }

    private val ceibroImagesPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val images = result.data?.extras?.getParcelableArrayList<PickedImages>("images")
                if (images != null) {
//                    val oldImages = viewModel.listOfImages.value
//                    oldImages?.addAll(images)
                    viewModel.listOfImages.postValue(images)
                } else {
                    viewModel.listOfImages.postValue(arrayListOf())
                }
            }
        }

    private fun showEditCommentDialog(data: PickedImages) {
        val sheet = EditCommentDialogSheet(data)
        sheet.updateCommentOnClick = { updatedComment ->
            val allImagesWithComment = viewModel.imagesWithComments.value
            val foundData = allImagesWithComment?.find { it.fileUri == data.fileUri }
            if (foundData != null) {
                val index = allImagesWithComment.indexOf(foundData)
                foundData.comment = updatedComment
                allImagesWithComment[index] = foundData
                viewModel.imagesWithComments.postValue(allImagesWithComment)
            }
        }

        sheet.isCancelable = false
        sheet.show(childFragmentManager, "EditCommentDialogSheet")
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
                GlobalScope.launch {
                    viewModel.loading(true)
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            val fileUri = clipData.getItemAt(i).uri
                            val fileName = getFileNameFromUri(requireContext(), fileUri)
                            var fileExtension = getFileExtension(requireContext(), fileUri)
                            if (fileExtension.isNullOrEmpty()) {
                                fileExtension = "jpg"
                            }
                            val newUri = createFileUriFromContentUri(
                                requireContext(),
                                fileUri,
                                fileName,
                                fileExtension!!
                            )
                            val file = FileUtils.getFile(requireContext(), newUri)
                            val selectedImgDetail =
                                getPickedFileDetail(requireContext(), newUri)

                            val foundImage =
                                oldImages?.find { oldImage -> oldImage.fileName == selectedImgDetail.fileName }
                            if (foundImage != null) {
                                viewModel.launch(Dispatcher.Main) {
                                    shortToastNow("You selected an already-added image")
                                }
                            } else {
                                val compressedImageFile =
                                    Compressor.compress(requireContext(), file) {
                                        quality(80)
                                        format(Bitmap.CompressFormat.JPEG)
                                    }
                                val compressedImageUri = Uri.fromFile(compressedImageFile)

                                if (compressedImageUri != null) {
                                    val selectedNewImgDetail =
                                        getPickedFileDetail(requireContext(), compressedImageUri)

                                    pickedImage.add(selectedNewImgDetail)
                                }
                            }
                        }
                    } else {
                        val fileUri = data.data
                        fileUri?.let {
                            val fileName = getFileNameFromUri(requireContext(), it)
                            var fileExtension = getFileExtension(requireContext(), fileUri)
                            if (fileExtension.isNullOrEmpty()) {
                                fileExtension = "jpg"
                            }
                            val newUri = createFileUriFromContentUri(
                                requireContext(),
                                it,
                                fileName,
                                fileExtension!!
                            )
                            val file = FileUtils.getFile(requireContext(), newUri)
                            val selectedImgDetail =
                                getPickedFileDetail(requireContext(), newUri)

                            val foundImage =
                                oldImages?.find { oldImage -> oldImage.fileName == selectedImgDetail.fileName }
                            if (foundImage != null) {
                                viewModel.launch(Dispatcher.Main) {
                                    shortToastNow("You selected an already-added image")
                                }
                            } else {
                                val compressedImageFile =
                                    Compressor.compress(requireContext(), file) {
                                        quality(80)
                                        format(Bitmap.CompressFormat.JPEG)
                                    }
                                val compressedImageUri = Uri.fromFile(compressedImageFile)

                                if (compressedImageUri != null) {
                                    val selectedNewImgDetail =
                                        getPickedFileDetail(requireContext(), compressedImageUri)

                                    pickedImage.add(selectedNewImgDetail)
                                }
                            }
                        }
                    }

                    val allOldImages = viewModel.listOfImages.value
                    allOldImages?.addAll(pickedImage)
                    viewModel.listOfImages.postValue(allOldImages)
                    viewModel.loading(false, "")
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
        println("mimeTypeFound: ${mimeType} - ${fileName}")
        val attachmentType = when {
            mimeType == null -> {
                AttachmentTypes.Doc
            }

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
                    ) ||
                    mimeType.equals(
                        "image/vnd.dwg",
                        true
                    ) ||
                    mimeType.equals(
                        "application/acad",
                        true
                    ) -> {
                AttachmentTypes.Doc
            }

            mimeType.contains("image/vnd") -> {
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

                VIEWER_REQUEST_CODE -> {

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
                         viewState.selfAssignedViewer.value = selfAssigned
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
                        viewState.selectedViewerContacts.value = selectedContactList
                    }
                    viewState.viewerText.value = assigneeMembers
                }

                TAG_REQUEST_CODE -> {

                }

                CONFIRMER_REQUEST_CODE -> {

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
                          viewState.selfAssignedConfermer.value = selfAssigned
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
                        viewState.selectedConfirmerContacts.value = selectedContactList
                    }
                    viewState.confirmerText.value = assigneeMembers
                }

                PROJECT_REQUEST_CODE -> {
                    val selectedProject =
                        result.data?.getParcelable<CeibroProjectV2>("project")
                    if (selectedProject != null) {
                        viewState.selectedProject.value = selectedProject
                        viewState.projectText.value = selectedProject.title
                    } else {
                        shortToastNow(resources.getString(R.string.project_not_selected))
                    }
                }

                DRAWING_REQUEST_CODE -> {
                    val pinLocationOnTask =
                        result.data?.getParcelable<AddLocationTask>("newLocationTaskData")
                    if (pinLocationOnTask != null) {
                        viewModel.newPinLocationInTask(pinLocationOnTask)
                    } else {
                        shortToastNow(resources.getString(R.string.unable_to_access_new_pin_data))
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

    private fun getBitmapFromFile(filePath: String): Bitmap? {
        val file = File(filePath)
        if (file.exists()) {
            // Decode the file into a Bitmap
            return BitmapFactory.decodeFile(file.absolutePath)
        }
        return null
    }

    private fun openAddLocationSheet() {
        val sheet = AddNewLocationBottomSheet(
            viewModel.originalAllGroups,
            viewModel.downloadedDrawingV2Dao,
            networkConnectivityObserver
        )

        sheet.onDrawingTapped = {
            sheet.dismiss()
            navigateForResult(R.id.viewDrawingV2Fragment, DRAWING_REQUEST_CODE)
        }

        sheet.isCancelable = true
        sheet.show(childFragmentManager, "AddNewLocationBottomSheet")
    }

    private val ceibroImageViewerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                var newList: ArrayList<PickedImages> = arrayListOf()
                val listOfPickedImages =
                    result.data?.extras?.getParcelableArrayList<PickedImages>("images")
                if (listOfPickedImages?.isNotEmpty() == true) {
                    newList = listOfPickedImages
                }

                val itemsToRemove = mutableListOf<PickedImages>()

                viewModel.listOfImages.value?.forEachIndexed { index, pickedImages ->
                    if (pickedImages.attachmentType == AttachmentTypes.Drawing) {
                        itemsToRemove.add(pickedImages)
                    }
                }
                newList.addAll(itemsToRemove)

                viewModel.listOfImages.postValue(newList)
            }
        }
}