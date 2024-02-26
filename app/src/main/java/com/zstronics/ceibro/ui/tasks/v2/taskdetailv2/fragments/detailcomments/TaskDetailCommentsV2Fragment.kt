package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailcomments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ahmadullahpk.alldocumentreader.activity.All_Document_Reader_Activity
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.viewmodel.Dispatcher
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.models.tasks.EventFiles
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.databinding.FragmentTaskDetailCommentsV2Binding
import com.zstronics.ceibro.extensions.openFilePicker
import com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawings.DrawingsV2Fragment
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.CeibroOnlyImageRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.EventsRVAdapter
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.CeibroCameraActivity
import ee.zstronics.ceibro.camera.CeibroImageViewerActivity
import ee.zstronics.ceibro.camera.FileUtils
import ee.zstronics.ceibro.camera.PickedImages
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class TaskDetailCommentsV2Fragment :
    BaseNavViewModelFragment<FragmentTaskDetailCommentsV2Binding, ITaskDetailCommentsV2.State, TaskDetailCommentsV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskDetailCommentsV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_detail_comments_v2
    override fun toolBarVisibility(): Boolean = false

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalZeroShutterLag::class)
    override fun onClick(id: Int) {
        when (id) {
            R.id.cameraBtn -> {
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

            R.id.attachmentBtn -> {
                chooseAttachment(
                    mimeTypes = arrayOf(
                        "text/plain",
                        "text/csv",
                        "application/pdf",
//                        "application/rtf",
//                        "application/zip",
                        "application/vnd.oasis.opendocument.text",                                  // .odt
                        "application/vnd.oasis.opendocument.spreadsheet",                           // .ods
                        "application/vnd.oasis.opendocument.presentation",                          // .odp
//                        "application/x-rar-compressed",
//                        "application/vnd.android.package-archive",      //for APK file
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",  // .docx
                        "application/vnd.ms-word.document.macroEnabled.12",                         // .doc
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",        // .xlsx
                        "application/vnd.ms-excel.sheet.macroEnabled.12",                           // .xls
                        "application/vnd.ms-powerpoint",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
                        "application/vnd.ms-powerpoint.presentation.macroEnabled.12",                 // .ppt
//                        "image/vnd.dwg",    // AutoCAD Drawing Database (DWG)
//                        "application/acad"  // AutoCAD Drawing
//                        "image/vnd.adobe.photoshop", // Photoshop Document (PSD)
                        "image/jpeg",
                        "image/jpg",
                        "image/png",
                        "image/gif",
                        "image/webp",
                        "image/bmp",
                        "image/*"
                    )
                )
            }

            R.id.sendMsgBtn -> {
                viewModel.uploadComment(
                    requireContext()
                ) { eventData ->
                    viewModel.listOfImages.postValue(arrayListOf())
                    viewModel.documents.postValue(arrayListOf())
                    viewState.comment.postValue("")
                    mViewDataBinding.msgTypingField.clearFocus()


//                    if (viewModel.taskFromNotification != null) {
//                        CeibroApplication.CookiesManager.taskIdInDetails = ""
//                        shortToastNow(getString(R.string.commented_successfully))
//                        val instances = countActivitiesInBackStack(requireContext())
//                        if (instances <= 1) {
//                            launchActivityWithFinishAffinity<NavHostPresenterActivity>(
//                                options = Bundle(),
//                                clearPrevious = true
//                            ) {
//                                putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
//                                putExtra(
//                                    NAVIGATION_Graph_START_DESTINATION_ID,
//                                    R.id.homeFragment
//                                )
//                            }
//                        } else {
//                            //finish is called so that second instance of app will be closed and only one last instance will remain
//                            finish()
//                        }
//                    } else {
//                        CeibroApplication.CookiesManager.taskIdInDetails = ""
//                        val bundle = Bundle()
//                        bundle.putParcelable("eventData", null)
//                        navigateBackWithResult(Activity.RESULT_OK, bundle)
//                    }
                }
            }
        }
    }

    private val ceibroImagesPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
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

    private val ceibroImageViewerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                var newList: ArrayList<PickedImages> = arrayListOf()
                val listOfPickedImages =
                    result.data?.extras?.getParcelableArrayList<PickedImages>("images")
                if (listOfPickedImages?.isNotEmpty() == true) {
                    newList = listOfPickedImages
                }
                viewModel.listOfImages.postValue(newList)
            }
        }


    private var manager: DownloadManager? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionList13 = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    private val permissionList10 = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )


    private lateinit var eventsAdapter: EventsRVAdapter

    @Inject
    lateinit var onlyImageAdapter: CeibroOnlyImageRVAdapter

    @Inject
    lateinit var filesAdapter: CeibroNewCommentFilesRVAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manager =
            mViewDataBinding.root.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        eventsAdapter = EventsRVAdapter(
            networkConnectivityObserver,
            requireContext(),
            viewModel.downloadedDrawingV2Dao
        )

        viewModel.taskEvents.observe(viewLifecycleOwner) { events ->
            if (!events.isNullOrEmpty()) {
                eventsAdapter.setList(
                    events,
                    viewModel.user?.id ?: viewModel.sessionManager.getUserObj()?.id ?: ""
                )
            } else {
                eventsAdapter.setList(
                    mutableListOf(),
                    viewModel.user?.id ?: viewModel.sessionManager.getUserObj()?.id ?: ""
                )
            }

            mViewDataBinding.eventsRV.visibility =
                if (events.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            Handler().postDelayed({
                eventsAdapter.itemCount.let { itemCount ->
                    if (itemCount > 0) {
                        mViewDataBinding.eventsRV.scrollToPosition(itemCount - 1)
                    }
                }
            }, 100)
        }
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        mViewDataBinding.eventsRV.layoutManager = layoutManager
        mViewDataBinding.eventsRV.adapter = eventsAdapter

        eventsAdapter.requestPermissionCallBack {
            checkDownloadFilePermission()
        }
        eventsAdapter.downloadFileCallBack { textView, ivDownload, downloaded, triplet, tag ->
            checkDownloadFilePermission(triplet, viewModel.downloadedDrawingV2Dao) {
                MainScope().launch {
                    /*
                          if (it.trim().equals("100%", true)) {
                              textView.visibility = View.GONE
                              downloaded.visibility = View.VISIBLE
                              textView.text = it
                              //    detailAdapter.notifyDataSetChanged()
                          } else if (it == "retry" || it == "failed") {
                              downloaded.visibility = View.GONE
                              textView.visibility = View.GONE
                              ivDownload.visibility = View.VISIBLE
                          } else {

                              println("progress: $it textView.text = ${textView.text}")
                              textView.text = it
                              textView.visibility = View.VISIBLE
                          }
                          */
                }
            }
        }

        eventsAdapter.fileClickListener =
            { view: View, position: Int, data: EventFiles, drawingFile ->
                val bundle = Bundle()
                bundle.putParcelable("eventFile", data)
                bundle.putParcelable("downloadedFile", drawingFile)

                val file = File(drawingFile.localUri)
                val fileUri = Uri.fromFile(file)
                val fileDetails = getPickedFileDetail(requireContext(), fileUri)
                if (fileDetails.attachmentType == AttachmentTypes.Pdf) {
                    navigate(R.id.fileViewerFragment, bundle)
                } else {
                    openFile(file, requireContext())
                    //    shortToastNow("File format not supported yet.")
                }
            }


        eventsAdapter.openEventImageClickListener =
            { _: View, position: Int, imageFiles: List<TaskFiles> ->
                val bundle = Bundle()
                bundle.putParcelableArray("images", imageFiles.toTypedArray())
                bundle.putInt("position", position)
                bundle.putBoolean("fromServerUrl", true)
                navigate(R.id.imageViewerFragment, bundle)
            }

        eventsAdapter.pinClickListener =
            { position, event, isPinned ->
                viewModel.pinOrUnpinComment(
                    event.taskId,
                    event.id,
                    isPinned
                ) { isSuccess, updatedEvent ->
                    if (updatedEvent != null) {
                        val originalEvents = viewModel.originalEvents.value
                        if (!originalEvents.isNullOrEmpty()) {
                            val foundEvent = originalEvents.find { it.id == updatedEvent.id }
                            if (foundEvent != null) {
                                val index = originalEvents.indexOf(foundEvent)
                                originalEvents[index] = updatedEvent
                                viewModel.originalEvents.postValue(originalEvents)
                            }
                        }

                        if (eventsAdapter.listItems.isNotEmpty()) {
                            val adapterEvent =
                                eventsAdapter.listItems.find { it.id == updatedEvent.id }
                            if (adapterEvent != null) {
                                val index = eventsAdapter.listItems.indexOf(adapterEvent)
                                eventsAdapter.listItems[index] = updatedEvent
                                eventsAdapter.notifyItemChanged(index)
                            }
                        }
                    }
                }
            }



        viewModel.missingEvents.observe(viewLifecycleOwner) { missingEventsList ->
            if (!missingEventsList.isNullOrEmpty()) {
                val taskEvents = viewModel.originalEvents.value
                taskEvents?.let { allEvents ->
                    GlobalScope.launch {
                        try {
                            if (allEvents.isNotEmpty()) {
                                val newMissingEventList = mutableListOf<Events>()
                                missingEventsList.forEach { event ->
                                    val eventExist = allEvents.find { event.id == it.id }
                                    if (eventExist == null) {  /// event not existed
                                        newMissingEventList.add(event)
                                    }
                                }
                                allEvents.addAll(newMissingEventList)
                                viewModel.originalEvents.postValue(allEvents)
                                viewModel._taskEvents.postValue(allEvents)


                            } else {
                                allEvents.addAll(missingEventsList)
                                viewModel.originalEvents.postValue(allEvents)
                                viewModel._taskEvents.postValue(allEvents)
                            }
                        } catch (e: Exception) {
                            println("missingEvents-Exception: $e")
                        }
                    }
                }
            }
        }


        viewModel.listOfImages.observe(viewLifecycleOwner) {
            onlyImageAdapter.setList(it)
            if (it.isNotEmpty()) {
                mViewDataBinding.newImagesRV.visibility = View.VISIBLE
            } else {
                mViewDataBinding.newImagesRV.visibility = View.GONE
            }
        }
        mViewDataBinding.newImagesRV.adapter = onlyImageAdapter
        onlyImageAdapter.openImageClickListener =
            { _: View, position: Int, fileUri: String, obj ->
                /* val bundle = Bundle()
                 bundle.putParcelableArray("images", viewModel.onlyImages.value?.toTypedArray())
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
                mViewDataBinding.newFilesRV.visibility = View.VISIBLE
            } else {
                mViewDataBinding.newFilesRV.visibility = View.GONE
            }
        }
        mViewDataBinding.newFilesRV.adapter = filesAdapter

        filesAdapter.itemClickListener = { _: View, position: Int, data: PickedImages ->
            val oldDocuments = viewModel.documents.value
            oldDocuments?.remove(data)
            viewModel.documents.postValue(oldDocuments)
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
    fun onTaskEvent(
        event: LocalEvents.TaskEvent?
    ) {
        val newEvent = event?.events
        val taskDetail = viewModel.taskDetail.value
        if (taskDetail != null && newEvent != null && newEvent.taskId == taskDetail.id) {
            addEventsToUI(newEvent)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskEventUpdate(
        event: LocalEvents.TaskEventUpdate?
    ) {
        val updatedEvent = event?.events
        val taskDetail = viewModel.taskDetail.value
        if (taskDetail != null && updatedEvent != null && updatedEvent.taskId == taskDetail.id) {

            val originalEvents = viewModel.originalEvents.value
            if (!originalEvents.isNullOrEmpty()) {
                val foundEvent = originalEvents.find { it.id == updatedEvent.id }
                if (foundEvent != null) {
                    val index = originalEvents.indexOf(foundEvent)
                    originalEvents[index] = updatedEvent
                    viewModel.originalEvents.postValue(originalEvents)
                }
            }

            if (eventsAdapter.listItems.isNotEmpty()) {
                val adapterEvent = eventsAdapter.listItems.find { it.id == updatedEvent.id }
                if (adapterEvent != null) {
                    val index = eventsAdapter.listItems.indexOf(adapterEvent)
                    eventsAdapter.listItems[index] = updatedEvent
                    eventsAdapter.notifyItemChanged(index)
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshAllEvents(
        event: LocalEvents.RefreshAllEvents?
    ) {
        viewModel.getAllEventsFromLocalEvents()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskSeenEvent(event: LocalEvents.TaskSeenEvent?) {
        val task = event?.task
        if (task != null) {
            viewModel.taskDetail.value?.let { taskDetail ->
                if (task.id == taskDetail.id) {
                    task.let { it1 ->
                        viewModel.originalTask.postValue(it1)
                        viewModel._taskDetail.postValue(it1)
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskDoneEvent(event: LocalEvents.TaskDoneEvent?) {
        val task = event?.task
        val taskEvent = event?.taskEvent
        val oldTaskDetail = viewModel.taskDetail.value
        if (task != null && taskEvent != null) {
            if (oldTaskDetail?.id == taskEvent.taskId) {
                val taskEvents = viewModel.originalEvents.value
                taskEvents?.let { allEvents ->
                    GlobalScope.launch {
                        if (allEvents.isNotEmpty()) {
                            val eventExist = allEvents.find { taskEvent.id == it.id }
                            if (eventExist == null) {  /// event not existed
                                allEvents.add(taskEvent)
                                viewModel.updateTaskAndAllEvents(taskEvent, allEvents)
//                            val handler = Handler(Looper.getMainLooper())
//                            handler.postDelayed(Runnable {
//                                eventsAdapter.listItems.add(taskEvent)
//                                mViewDataBinding.eventsRV.adapter?.notifyItemInserted(eventsAdapter.listItems.size - 1)
//                                scrollToBottom(allEvents.size)
//                            }, 1)

                            }
                        } else {
                            val eventList = mutableListOf<Events>()
                            eventList.add(taskEvent)
                            allEvents.addAll(eventList)
                            viewModel.updateTaskAndAllEvents(taskEvent, allEvents)
//                            scrollToBottom(allEvents.size)
                        }
                    }
                }
            }
        }
    }


    private fun addEventsToUI(taskEvent: Events) {
        val taskEvents = viewModel.originalEvents.value
        taskEvents?.let { allEvents ->
            viewModel.launch {
                if (allEvents.isNotEmpty()) {
                    val eventExist = allEvents.find { taskEvent.id == it.id }
                    if (eventExist == null) {  /// event not existed
                        allEvents.add(taskEvent)
                        viewModel.updateTaskAndAllEvents(taskEvent, allEvents)

//                        val handler = Handler(Looper.getMainLooper())
//                        handler.postDelayed(Runnable {
//                            eventsAdapter.listItems.add(taskEvent)
//                            mViewDataBinding.eventsRV.adapter?.notifyItemInserted(eventsAdapter.listItems.size - 1)
//                        scrollToBottom(allEvents.size)
//                        }, 1)

                    }
                } else {
                    val eventList = mutableListOf<Events>()
                    eventList.add(taskEvent)
                    allEvents.addAll(eventList)
                    viewModel.updateTaskAndAllEvents(taskEvent, allEvents)
//                    val task = viewModel.taskDao.getTaskByID(taskEvent.taskId)
//                    task?.let {
//                        viewModel.originalTask.postValue(it)
//                        viewModel._taskDetail.postValue(it)
//
//                        detailAdapter.updateTaskData(it)
//                    }
//                    viewModel.originalEvents.postValue(allEvents)
//                    viewModel._taskEvents.postValue(allEvents)
//                    scrollToBottom(allEvents.size)
//                    val seenByMe = task?.seenBy?.find { it == viewModel.user?.id }
//                    if (seenByMe == null) {
//                        viewModel.taskSeen(taskEvent.taskId) { }
//                    }
                }
            }

        }
    }


    private fun scrollToBottom(size: Int) {
//        if (!isScrolling) {
//            isScrolling = true
        var scrollDelay: Long = 100
//        if (size >= 40 && size < 52) {
//            scrollDelay = 450
//        } else if (size >= 52) {
//            scrollDelay = 530
//        }
        Handler(Looper.getMainLooper()).postDelayed({
            //   mViewDataBinding.bodyScroll.isFocusableInTouchMode = true
            //  mViewDataBinding.bodyScroll.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
            //   mViewDataBinding.bodyScroll.requestLayout()
            // mViewDataBinding.bodyScroll.scrollToBottomWithoutFocusChange()
//            mViewDataBinding.bodyScroll.fullScroll(View.FOCUS_DOWN)
//            isScrolling = false
            eventsAdapter.itemCount.let { itemCount ->
                if (itemCount > 0) {
                    mViewDataBinding.eventsRV.scrollToPosition(itemCount - 1)
                }
            }
        }, scrollDelay)
//        }
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

            mimeType == "application/x-rar-compressed" || mimeType == "application/zip" -> {
                AttachmentTypes.Zip
            }

            mimeType.equals("text/plain", true) ||
                    mimeType.equals("text/csv", true) ||
                    mimeType.equals("application/rtf", true) ||
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

    private fun openFile(file: File, context: Context) {

        val intent = Intent(context, All_Document_Reader_Activity::class.java)
        intent.putExtra("path", file.absolutePath)
        intent.putExtra("fromAppActivity", true)
        context.startActivity(intent)
        return

    }

    private fun checkDownloadFilePermission(
        url: Triple<String, String, String>,
        downloadedDrawingV2Dao: DownloadedDrawingV2Dao,
        itemClickListener: ((tag: String) -> Unit)?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkPermissions(permissionList13)) {
                downloadFile(url, downloadedDrawingV2Dao) {
                    itemClickListener?.invoke(it)
                }
            } else {
                requestPermissions(permissionList13)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {

            downloadFile(url, downloadedDrawingV2Dao) {
                itemClickListener?.invoke(it)
            }
        } else {
            if (checkPermissions(permissionList10)) {
                downloadFile(url, downloadedDrawingV2Dao) {
                    itemClickListener?.invoke(it)
                }
            } else {

                downloadFile(url, downloadedDrawingV2Dao) {
                    itemClickListener?.invoke(it)
                }
                requestPermissions(permissionList10)
            }
        }
    }

    private fun checkDownloadFilePermission(

    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(permissionList13)

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {


        } else {
            requestPermissions(permissionList10)
        }
    }

    private fun checkPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(
                requireContext(),
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions(permissions: Array<String>) {
        ActivityCompat.requestPermissions(
            requireActivity(), permissions,
            DrawingsV2Fragment.permissionRequestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == DrawingsV2Fragment.permissionRequestCode) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted
            } else {
                handleDeniedPermissions(permissions, grantResults)
            }
        }
    }

    private fun handleDeniedPermissions(permissions: Array<out String>, grantResults: IntArray) {
        for (i in permissions.indices) {
            val permission = permissions[i]
            val result = grantResults[i]

            if (result == PackageManager.PERMISSION_DENIED) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    showToast("Permission denied: $permission")
                } else {
                    showToast("Permission denied: $permission. Please enable it in the app settings.")
                    navigateToAppSettings(context)
                    return
                }
            }
        }
    }

    private fun navigateToAppSettings(context: Context?) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", context?.packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun downloadFile(
        triplet: Triple<String, String, String>,
        downloadedDrawingV2Dao: DownloadedDrawingV2Dao,
        itemClickListener: ((tag: String) -> Unit)?
    ) {


        manager?.let {
            downloadGenericFile(triplet, downloadedDrawingV2Dao, it) { downloadId ->
                Handler(Looper.getMainLooper()).postDelayed({
                    getDownloadProgress(context, downloadId) {tag->
                        GlobalScope.launch(Dispatchers.Main) {
                            if (tag == "retry" || tag == "failed") {
                                downloadedDrawingV2Dao.deleteByDrawingID(downloadId.toString())
                            } else if (tag.trim().equals("100%", true)) {

                                shortToastNow("Downloaded")
                            }
                        }
                        itemClickListener?.invoke(tag)
                    }
                }, 1000)
            }
        } ?: kotlin.run {

            manager =
                requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            manager?.let {
                downloadGenericFile(triplet, downloadedDrawingV2Dao, it) { downloadId ->
                    Handler(Looper.getMainLooper()).postDelayed({
                        getDownloadProgress(context, downloadId) {tag->
                            GlobalScope.launch(Dispatchers.Main) {
                                if (tag == "retry" || tag == "failed") {
                                    downloadedDrawingV2Dao.deleteByDrawingID(downloadId.toString())
                                } else if (tag.trim().equals("100%", true)) {

                                    shortToastNow("Downloaded")
                                }
                            }
                            itemClickListener?.invoke(tag)
                        }
                    }, 1000)
                }
            }
        }

        /*shortToastNow("Downloading file...")
        val uri = Uri.parse(triplet.third)
        val fileName = triplet.second
        val folder = File(
            context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            DrawingsV2Fragment.folderName
        )
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val destinationUri = Uri.fromFile(File(folder, fileName))

        val request: DownloadManager.Request? =
            DownloadManager
                .Request(uri)
                .setDestinationUri(destinationUri)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setVisibleInDownloadsUi(true)

        val downloadId = manager?.enqueue(request)

        println("progress id   $downloadId")
        println("progress   $manager")

        val ceibroDownloadDrawingV2 = downloadId?.let {
            CeibroDownloadDrawingV2(
                fileName = triplet.second,
                downloading = true,
                isDownloaded = false,
                downloadId = it,
                drawing = null,
                drawingId = triplet.first,
                groupId = "",
                localUri = ""
            )
        }


        GlobalScope.launch {
            ceibroDownloadDrawingV2?.let {
                downloadedDrawingV2Dao.insertDownloadDrawing(it)
                println("progress  object  $it")
            }

        }*/


//        println("id: ${id} Folder name: ${folder} uri:${uri} destinationUri:${destinationUri}")

    }

    @SuppressLint("Range")
    private fun getDownloadProgressSeparately(
        context: Context?,
        downloadId: Long,
        itemClickListener: ((tag: String) -> Unit)?
    ) {
        GlobalScope.launch {
            while (true) {
                println("progress : checking")
                val query = DownloadManager.Query().setFilterById(downloadId)
                val manager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val cursor = manager.query(query)

                if (cursor.moveToFirst()) {
                    val bytesDownloaded =
                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val bytesTotal =
                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                    val status =
                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    println("Status: $status")

                    println("progress status : $status")

                    if (status.toInt() == DownloadManager.STATUS_FAILED) {

                        println("Status failed: $status")
                        itemClickListener?.invoke("failed")
                        break
                    } else if (status.toInt() == DownloadManager.STATUS_SUCCESSFUL) {
                        itemClickListener?.invoke("100%")
                        break
                    }

                    val downloadedPercent = ((bytesDownloaded * 100L) / bytesTotal).toInt()

                    println("StatusProgress %: $downloadedPercent")
                    println("StatusDownloaded: $bytesDownloaded")
                    println("StatusTotal: $bytesTotal")

                    itemClickListener?.invoke("$downloadedPercent %")
                    if (bytesTotal > 0) {
                        println("Progress: " + ((bytesDownloaded * 100L) / bytesTotal).toInt())
                        println("progress downloaded" + ((bytesDownloaded * 100L) / bytesTotal).toInt())
                    }
                } else {
                    println("progress retry ")
                    itemClickListener?.invoke("retry")
                    break
                }
                cursor.close()

                delay(500)
            }
        }
    }

    @SuppressLint("Range")
    private fun getDownloadProgress(
        context: Context?,
        downloadId: Long,
        itemClickListener: ((tag: String) -> Unit)?
    ) {
        GlobalScope.launch {
            while (true) {
                println("progress : checking")
                val query = DownloadManager.Query().setFilterById(downloadId)
                val manager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val cursor = manager.query(query)

                if (cursor.moveToFirst()) {
                    val bytesDownloaded =
                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    val bytesTotal =
                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

                    val status =
                        cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    println("Status: $status")

                    println("progress status : $status")

                    if (status.toInt() == DownloadManager.STATUS_FAILED) {

                        println("Status failed: $status")
                        itemClickListener?.invoke("failed")
                        break
                    } else if (status.toInt() == DownloadManager.STATUS_SUCCESSFUL) {
                        itemClickListener?.invoke("100%")
                        break
                    }

                    val downloadedPercent = ((bytesDownloaded * 100L) / bytesTotal).toInt()

                    println("StatusProgress %: $downloadedPercent")
                    println("StatusDownloaded: $bytesDownloaded")
                    println("StatusTotal: $bytesTotal")

                    itemClickListener?.invoke("$downloadedPercent %")
                    if (bytesTotal > 0) {
                        println("Progress: " + ((bytesDownloaded * 100L) / bytesTotal).toInt())
                        println("progress downloaded" + ((bytesDownloaded * 100L) / bytesTotal).toInt())
                    }
                } else {
                    println("progress retry ")
                    itemClickListener?.invoke("retry")
                    break
                }
                cursor.close()

                delay(500)
            }
        }
    }


    private fun chooseAttachment(mimeTypes: Array<String>) {
        requireActivity().openFilePicker(
            mimeTypes = mimeTypes,
            allowMultiple = true
        ) { resultCode, data ->
            val pickedDocuments = arrayListOf<PickedImages>()
            val pickedImage = arrayListOf<PickedImages>()
            val oldDocuments = viewModel.documents.value
            val oldImages = viewModel.listOfImages.value

            GlobalScope.launch {
                viewModel.loading(true)
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val clipData = data.clipData
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            val fileUri = clipData.getItemAt(i).uri
                            val selectedDocDetail = getPickedFileDetail(requireContext(), fileUri)

                            if (selectedDocDetail.attachmentType == AttachmentTypes.Doc || selectedDocDetail.attachmentType == AttachmentTypes.Pdf) {
                                if (oldDocuments?.contains(selectedDocDetail) == true) {
                                    shortToastNow("You selected an already-added document")
                                } else {
                                    pickedDocuments.add(selectedDocDetail)
                                }
                            } else if (selectedDocDetail.attachmentType == AttachmentTypes.Image) {
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
                                            getPickedFileDetail(
                                                requireContext(),
                                                compressedImageUri
                                            )

                                        pickedImage.add(selectedNewImgDetail)
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    shortToastNow("One of the file has file-type unknown")
                                }
                            }

                        }
                    } else {
                        val fileUri = data.data
                        fileUri?.let {
                            val selectedDocDetail = getPickedFileDetail(requireContext(), it)

                            if (selectedDocDetail.attachmentType == AttachmentTypes.Doc || selectedDocDetail.attachmentType == AttachmentTypes.Pdf) {
                                if (oldDocuments?.contains(selectedDocDetail) == true) {
                                    shortToastNow("You selected an already-added document")
                                } else {
                                    pickedDocuments.add(selectedDocDetail)
                                }
                            } else if (selectedDocDetail.attachmentType == AttachmentTypes.Image) {
                                val fileName = getFileNameFromUri(requireContext(), it)
                                var fileExtension = getFileExtension(requireContext(), it)
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
                                            getPickedFileDetail(
                                                requireContext(),
                                                compressedImageUri
                                            )

                                        pickedImage.add(selectedNewImgDetail)
                                    } else {

                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    shortToastNow("Selected file has unknown file-type")
                                }
                            }
                        }
                    }
                }

                val allOldImages = viewModel.listOfImages.value
                allOldImages?.addAll(pickedImage)
                viewModel.listOfImages.postValue(allOldImages)

                oldDocuments?.addAll(pickedDocuments)
                viewModel.documents.postValue(oldDocuments)
                viewModel.loading(false, "")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!viewModel.isResumedCalled) {
            println("fragment : resumed called")
            viewModel.isResumedCalled = true
            viewModel.taskDetail.value?.let {
                viewModel.syncEvents(it.id)
            }

        }
    }

}