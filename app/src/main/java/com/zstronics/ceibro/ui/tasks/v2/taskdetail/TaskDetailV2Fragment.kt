package com.zstronics.ceibro.ui.tasks.v2.taskdetail

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import com.ahmadullahpk.alldocumentreader.activity.All_Document_Reader_Activity
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.finish
import com.zstronics.ceibro.base.extensions.launchActivityWithFinishAffinity
import com.zstronics.ceibro.base.extensions.scrollToBottomWithoutFocusChange
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BackNavigationResult
import com.zstronics.ceibro.base.navgraph.BackNavigationResultListener
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroDownloadDrawingV2
import com.zstronics.ceibro.data.database.models.tasks.Events
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.v2.EventV2Response
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.databinding.FragmentTaskDetailV2Binding
import com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawings.DrawingsV2Fragment
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter.TaskDetailV2RVAdapter
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.FileUtils
import ee.zstronics.ceibro.camera.PickedImages
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File


@AndroidEntryPoint
class TaskDetailV2Fragment :
    BaseNavViewModelFragment<FragmentTaskDetailV2Binding, ITaskDetailV2.State, TaskDetailV2VM>(),
    BackNavigationResultListener {
    var isScrollingWithDelay = false
    private var manager: DownloadManager? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val permissionList13 = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    private val permissionList10 = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )


    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskDetailV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_detail_v2
    override fun toolBarVisibility(): Boolean = false
    val FORWARD_TASK_REQUEST_CODE = 104
    val COMMENT_REQUEST_CODE = 106
    val DONE_REQUEST_CODE = 107
    var taskSeenRequest = false
    private var isScrolling = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.closeBtn -> {
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

            R.id.taskInfoBtn -> showTaskInfoBottomSheet()
            R.id.DrawingOpenBtn -> {
                showToast("Coming soon !!!")
            }

            R.id.taskCommentBtn -> {
                val bundle = Bundle()
                val taskData = viewModel.taskDetail.value
                bundle.putBoolean("doneCommentsRequired", taskData?.doneCommentsRequired ?: false)
                bundle.putBoolean("doneImageRequired", taskData?.doneImageRequired ?: false)
                bundle.putString("taskId", taskData?.id)
                bundle.putString("action", TaskDetailEvents.Comment.eventValue)
                navigateForResult(R.id.commentFragment, COMMENT_REQUEST_CODE, bundle)
            }

            R.id.doneBtn -> {
                if (viewModel.taskDetail.value?.doneCommentsRequired == true || viewModel.taskDetail.value?.doneImageRequired == true) {
                    val bundle = Bundle()
                    val taskData = viewModel.taskDetail.value
                    bundle.putBoolean(
                        "doneCommentsRequired",
                        taskData?.doneCommentsRequired ?: false
                    )
                    bundle.putBoolean("doneImageRequired", taskData?.doneImageRequired ?: false)
                    bundle.putString("taskId", taskData?.id)
                    bundle.putString("action", TaskDetailEvents.DoneTask.eventValue)
                    navigateForResult(R.id.commentFragment, DONE_REQUEST_CODE, bundle)
                } else {
                    viewModel.doneTask(viewModel.taskDetail.value?.id ?: "") {
//                        if (task != null) {
//                            viewModel.originalTask.postValue(task)
//                            viewModel._taskDetail.postValue(task)
//                        }
                    }
                }
            }

            R.id.taskForwardBtn -> {
                val taskData = viewModel.taskDetail.value
                val assignTo = taskData?.assignedToState?.map { it.phoneNumber }
                val invited = taskData?.invitedNumbers?.map { it.phoneNumber }
                val combinedList = arrayListOf<String>()
                if (assignTo != null) {
                    combinedList.addAll(assignTo)
                }
                if (invited != null) {
                    combinedList.addAll(invited)
                }

                val bundle = Bundle()
                bundle.putStringArrayList(
                    "assignToContacts",
                    combinedList
                )
                bundle.putString("taskId", taskData?.id)
                navigateForResult(R.id.forwardTaskFragment, FORWARD_TASK_REQUEST_CODE, bundle)
            }

        }
    }


    lateinit var detailAdapter: TaskDetailV2RVAdapter
    private var eventAdapterIsSet = false

    //This function is called when fragment is closed and detach from activity
    override fun onDetach() {
        CeibroApplication.CookiesManager.taskDataForDetails = null
        CeibroApplication.CookiesManager.taskDetailEvents = null
        CeibroApplication.CookiesManager.taskDetailRootState = null
        CeibroApplication.CookiesManager.taskDetailSelectedSubState = null
        super.onDetach()
    }

    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
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

        manager =
            mViewDataBinding.root.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        mViewDataBinding.confirmNeededBtn.visibility = View.GONE

        detailAdapter = TaskDetailV2RVAdapter(
            networkConnectivityObserver,
            mViewDataBinding.root.context,
            viewModel.downloadedDrawingV2Dao
        )
        detailAdapter.requestPermissionCallBack {
            checkDownloadFilePermission()
        }

        detailAdapter.downloadFileCallBack { textView, ivDownload, downloaded, triplet, tag ->
            checkDownloadFilePermission(triplet, viewModel.downloadedDrawingV2Dao) {
                MainScope().launch {
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
                }
            }
        }


        mViewDataBinding.parentRV.adapter = detailAdapter
        mViewDataBinding.parentRV.itemAnimator = DefaultItemAnimator()

        detailAdapter.openEventImageClickListener = { position, bundle ->
            navigate(R.id.imageViewerFragment, bundle)
        }

        detailAdapter.fileViewerClickListener = { position, bundle, downloadedData ->

            val file = File(downloadedData.localUri)
            val fileUri = Uri.fromFile(file)
            val fileDetails = getPickedFileDetail(requireContext(), fileUri)
            if (fileDetails.attachmentType == AttachmentTypes.Pdf) {
                navigate(R.id.fileViewerFragment, bundle)
            } else {
                openFile(file, requireContext())
                //    shortToastNow("File format not supported yet.")
            }


        }

        detailAdapter.descriptionExpendedListener = { expanded ->
            viewModel.descriptionExpanded = expanded
            if (!expanded) {
                mViewDataBinding.bodyScroll.fullScroll(View.FOCUS_UP)
            }
        }


        viewModel.notificationTaskData.observe(viewLifecycleOwner) { notificationData ->
            if (notificationData != null) {
                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
            }
        }

        viewModel.isTaskBeingDone.observe(viewLifecycleOwner) {
//            if (notificationData != null) {
//                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
//            }
            if (it) {
                mViewDataBinding.doneBtn.isEnabled = false
                mViewDataBinding.doneBtn.isClickable = false
                mViewDataBinding.doneBtn.alpha = 0.6f
                mViewDataBinding.taskForwardBtn.isEnabled = false
                mViewDataBinding.taskForwardBtn.isClickable = false
                mViewDataBinding.taskForwardBtn.alpha = 0.6f
            }
        }

        viewModel.taskDetail.observe(viewLifecycleOwner) { item ->
            if (item.hasPinData) {
                mViewDataBinding.DrawingOpenBtn.visibility = View.VISIBLE
            } else {
                mViewDataBinding.DrawingOpenBtn.visibility = View.GONE
            }

            if (taskSeenRequest) {
                taskSeenRequest = false
                detailAdapter.updateTaskData(item, viewModel.descriptionExpanded)
            }

            if (item != null) {
                if (item.creatorState.equals(
                        TaskStatus.DONE.name,
                        true
                    ) || item.creatorState.equals(
                        TaskStatus.CANCELED.name,
                        true
                    ) ||
                    (viewModel.rootState == TaskRootStateTags.ToMe.tagValue && (item.assignedToState.find { it.userId == viewModel.user?.id }?.state).equals(
                        TaskStatus.NEW.name,
                        true
                    ))
                ) {
                    mViewDataBinding.doneBtn.isEnabled = false
                    mViewDataBinding.doneBtn.isClickable = false
                    mViewDataBinding.doneBtn.alpha = 0.6f
                    mViewDataBinding.taskForwardBtn.isEnabled = false
                    mViewDataBinding.taskForwardBtn.isClickable = false
                    mViewDataBinding.taskForwardBtn.alpha = 0.6f
                } else {
                    if (viewModel.isTaskBeingDone.value == false) {
                        mViewDataBinding.doneBtn.isEnabled = true
                        mViewDataBinding.doneBtn.isClickable = true
                        mViewDataBinding.doneBtn.alpha = 1f
                        mViewDataBinding.taskForwardBtn.isEnabled = true
                        mViewDataBinding.taskForwardBtn.isClickable = true
                        mViewDataBinding.taskForwardBtn.alpha = 1f
                    }
                }
                if (item.creatorState.equals(
                        TaskStatus.DONE.name,
                        true
                    ) || item.creatorState.equals(
                        TaskStatus.CANCELED.name,
                        true
                    )
                ) {
                    mViewDataBinding.doneRequirementBadge.visibility = View.GONE
                } else {
                    if (item.doneCommentsRequired || item.doneImageRequired) {
                        mViewDataBinding.doneRequirementBadge.visibility = View.VISIBLE
                    } else {
                        mViewDataBinding.doneRequirementBadge.visibility = View.GONE
                    }
                }

                mViewDataBinding.detailViewHeading.text = item.taskUID
            }

            /*if (item != null) {
                if (item.creatorState.equals(
                        TaskStatus.DONE.name,
                        true
                    ) || item.creatorState.equals(
                        TaskStatus.CANCELED.name,
                        true
                    ) ||
                    (viewModel.rootState == TaskRootStateTags.ToMe.tagValue && (item.assignedToState.find { it.userId == viewModel.user?.id }?.state).equals(
                        TaskStatus.NEW.name,
                        true
                    ))
                ) {
                    mViewDataBinding.doneBtn.isEnabled = false
                    mViewDataBinding.doneBtn.isClickable = false
                    mViewDataBinding.doneBtn.alpha = 0.6f
                    mViewDataBinding.taskForwardBtn.isEnabled = false
                    mViewDataBinding.taskForwardBtn.isClickable = false
                    mViewDataBinding.taskForwardBtn.alpha = 0.6f
                } else {
                    mViewDataBinding.doneBtn.isEnabled = true
                    mViewDataBinding.doneBtn.isClickable = true
                    mViewDataBinding.doneBtn.alpha = 1f
                    mViewDataBinding.taskForwardBtn.isEnabled = true
                    mViewDataBinding.taskForwardBtn.isClickable = true
                    mViewDataBinding.taskForwardBtn.alpha = 1f
                }
                if (item.creatorState.equals(
                        TaskStatus.DONE.name,
                        true
                    ) || item.creatorState.equals(
                        TaskStatus.CANCELED.name,
                        true
                    )
                ) {
                    mViewDataBinding.doneRequirementBadge.visibility = View.GONE
                } else {
                    if (item.doneCommentsRequired || item.doneImageRequired) {
                        mViewDataBinding.doneRequirementBadge.visibility = View.VISIBLE
                    } else {
                        mViewDataBinding.doneRequirementBadge.visibility = View.GONE
                    }
                }

                mViewDataBinding.detailViewHeading.text = item.taskUID

                var state = ""
                state =
                    if (viewModel.rootState == TaskRootStateTags.FromMe.tagValue && viewModel.user?.id == item.creator.id) {
                        item.creatorState
                    } else if (viewModel.rootState == TaskRootStateTags.Hidden.tagValue && viewModel.selectedState.equals(
                            TaskStatus.CANCELED.name,
                            true
                        )
                    ) {
                        item.creatorState
                    } else {
                        item.assignedToState.find { it.userId == viewModel.user?.id }?.state ?: ""
                    }
                val taskStatusNameBg: Pair<Int, String> = when (state.uppercase()) {
                    TaskStatus.NEW.name -> Pair(
                        R.drawable.status_new_filled_more_corners,
                        requireContext().getString(R.string.new_heading)
                    )

                    TaskStatus.UNREAD.name -> Pair(
                        R.drawable.status_new_filled_more_corners,
                        requireContext().getString(R.string.unread_heading)
                    )

                    TaskStatus.ONGOING.name -> Pair(
                        R.drawable.status_ongoing_filled_more_corners,
                        requireContext().getString(R.string.ongoing_heading)
                    )

                    TaskStatus.DONE.name -> Pair(
                        R.drawable.status_done_filled_more_corners,
                        requireContext().getString(R.string.done_heading)
                    )

                    TaskStatus.CANCELED.name -> Pair(
                        R.drawable.status_cancelled_filled_more_corners,
                        requireContext().getString(R.string.canceled)
                    )

                    else -> Pair(
                        R.drawable.status_draft_outline,
                        state.ifEmpty {
                            "N/A"
                        }
                    )
                }
                val (background, status) = taskStatusNameBg
                mViewDataBinding.taskDetailStatusName.setBackgroundResource(background)
                mViewDataBinding.taskDetailStatusName.text = status

                mViewDataBinding.taskDetailCreationDate.text =
                    DateUtils.formatCreationUTCTimeToCustom(
                        utcTime = item.createdAt,
                        inputFormatter = DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC
                    )

                var dueDate = ""
                dueDate = DateUtils.reformatStringDate(
                    date = item.dueDate,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT
                )
                if (dueDate == "") {                              // Checking if date format was not dd-MM-yyyy then it will be empty
                    dueDate = DateUtils.reformatStringDate(
                        date = item.dueDate,
                        DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT,
                        DateUtils.FORMAT_SHORT_DATE_MON_YEAR_WITH_DOT
                    )
                    if (dueDate == "") {
                        dueDate = "N/A"
                    }
                }
                mViewDataBinding.taskDetailDueDate.text = "Due Date: $dueDate"

                mViewDataBinding.taskTitle.text =
                    if (item.topic != null) {
                        item.topic.topic.ifEmpty {
                            "N/A"
                        }
                    } else {
                        "N/A"
                    }

                if (item.description.isNotEmpty()) {
                    mViewDataBinding.taskDescription.text = item.description
                } else {
                    mViewDataBinding.taskDescription.text = ""
                    mViewDataBinding.taskDescription.visibility = View.GONE
                }
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    if (mViewDataBinding.taskDescription.lineCount > 15) {
                        mViewDataBinding.viewMoreLessLayout.visibility = View.VISIBLE
                        mViewDataBinding.viewMoreBtn.visibility = View.VISIBLE
                        mViewDataBinding.viewLessBtn.visibility = View.GONE
                    } else {
                        mViewDataBinding.viewMoreLessLayout.visibility = View.GONE
                        mViewDataBinding.viewMoreBtn.visibility = View.GONE
                        mViewDataBinding.viewLessBtn.visibility = View.GONE
                    }
                }, 25)

                if (item.files.isNotEmpty()) {
                    viewModel.separateFiles(item.files)
                }

//            if (item.events.isNotEmpty()) {
//                mViewDataBinding.eventsLayout.visibility = View.VISIBLE
//                viewModel.handleEvents(item.events)
//            } else {
//                mViewDataBinding.eventsLayout.visibility = View.GONE
//            }
            } else {
                shortToastNow("Task Data is empty")
            }*/
        }


        /*viewModel.onlyImages.observe(viewLifecycleOwner) {
            onlyImageAdapter.setList(it)
            mViewDataBinding.onlyImagesRV.visibility =
                if (it.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
        mViewDataBinding.onlyImagesRV.adapter = onlyImageAdapter
        onlyImageAdapter.openImageClickListener =
            { _: View, position: Int, fileUrl: String ->
//                val fileUrls: ArrayList<String> = viewModel.onlyImages.value?.map { it.fileUrl } as ArrayList<String>
//                viewModel.openImageViewer(requireContext(), fileUrls, position)
                val bundle = Bundle()
                bundle.putParcelableArray("images", viewModel.onlyImages.value?.toTypedArray())
                bundle.putInt("position", position)
                bundle.putBoolean("fromServerUrl", true)
                navigate(R.id.imageViewerFragment, bundle)
            }*/


        /*viewModel.imagesWithComments.observe(viewLifecycleOwner) {
            imageWithCommentAdapter.setList(it)
            mViewDataBinding.imagesWithCommentRV.visibility =
                if (it.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
        mViewDataBinding.imagesWithCommentRV.adapter = imageWithCommentAdapter
        imageWithCommentAdapter.openImageClickListener =
            { _: View, position: Int, fileUrl: String ->
//                val fileUrls: ArrayList<String> = viewModel.imagesWithComments.value?.map { it.fileUrl } as ArrayList<String>
//                viewModel.openImageViewer(requireContext(), fileUrls, position)
                val bundle = Bundle()
                bundle.putParcelableArray(
                    "images",
                    viewModel.imagesWithComments.value?.toTypedArray()
                )
                bundle.putInt("position", position)
                bundle.putBoolean("fromServerUrl", true)
                navigate(R.id.imageViewerFragment, bundle)
            }*/

        /*viewModel.documents.observe(viewLifecycleOwner) {
            filesAdapter.setList(it)
            mViewDataBinding.filesLayout.visibility =
                if (it.isNotEmpty()) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            mViewDataBinding.filesCount.text = "${it.size} file(s)"
        }
        mViewDataBinding.filesRV.adapter = filesAdapter
        filesAdapter.fileClickListener = { _: View, position: Int, data: TaskFiles ->
            val bundle = Bundle()
            bundle.putParcelable("taskFile", data)
            navigate(R.id.fileViewerFragment, bundle)
//            val pdfUrl = data.fileUrl             // This following code downloads the file
//            val intent: Intent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
//                .addCategory(Intent.CATEGORY_BROWSABLE)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            context?.startActivity(intent)
        }*/


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


//                                val handler = Handler(Looper.getMainLooper())
//                                handler.postDelayed(Runnable {
//                                    val startPosition = eventsAdapter.listItems.size
//                                    val itemCount = newMissingEventList.size
//
//                                    eventsAdapter.listItems.addAll(newMissingEventList)
//                                    mViewDataBinding.eventsRV.adapter?.notifyItemRangeInserted(
//                                        startPosition,
//                                        itemCount
//                                    )
//                                }, 10)
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
//        if (!eventAdapterIsSet) {
//            mViewDataBinding.eventsRV.adapter = eventsAdapter

//            val layoutManager = LinearLayoutManager(context)
//            layoutManager.isAutoMeasureEnabled = false      //to show all content in RV
//            mViewDataBinding.eventsRV.layoutManager = layoutManager
//        }

//        println("RecyclerView Detached Or Not: ${mViewDataBinding.eventsRV.adapter}")
        viewModel.taskEvents.observe(viewLifecycleOwner) { events ->
            detailAdapter.setOtherData(
                viewModel.rootState,
                viewModel.selectedState,
                viewModel.descriptionExpanded
            )

            if (!events.isNullOrEmpty()) {
                if (!eventAdapterIsSet) {
                    viewModel.sessionManager.getUser().value?.id?.let { userId ->
                        val task = viewModel.taskDetail.value
                        task?.let { taskData ->
                            val mixedList: MutableList<Any> = mutableListOf(taskData, events)

                            detailAdapter.setTaskAndEventList(
                                mixedList,
                                userId,
                                viewModel.descriptionExpanded
                            )
                        }
                    }
                    eventAdapterIsSet = true
                } else {
                    viewModel.sessionManager.getUser().value?.id?.let { userId ->
                        val task = viewModel.taskDetail.value
                        task?.let { taskData ->
                            val mixedList: MutableList<Any> = mutableListOf(taskData, events)

                            detailAdapter.updateTaskAndEventList(
                                mixedList,
                                userId,
                                viewModel.descriptionExpanded
                            )
                        }
                    }
                }
            } else {
                viewModel.sessionManager.getUser().value?.id?.let { userId ->
                    val task = viewModel.taskDetail.value
                    task?.let { taskData ->
                        val mixedList: MutableList<Any> =
                            mutableListOf(taskData, mutableListOf<Events>())

                        detailAdapter.setTaskAndEventList(
                            mixedList,
                            userId,
                            viewModel.descriptionExpanded
                        )
                    }
                }
            }


//            if (!it.isNullOrEmpty() && it.size == viewModel.originalEvents.value?.size) {
//                viewModel.sessionManager.getUser().value?.id?.let { userId ->
//                    eventsAdapter.setList(
//                        it,
//                        userId
//                    )
//                }
//                mViewDataBinding.eventsLayout.visibility =
//                    if (it.isNotEmpty()) {
//                        View.VISIBLE
//                    } else {
//                        View.GONE
//                    }
//                eventAdapterIsSet = true
//            } else {
//                val allEvents = viewModel.originalEvents.value
//                if (!allEvents.isNullOrEmpty()) {
//                    viewModel.sessionManager.getUser().value?.id?.let { userId ->
//                        eventsAdapter.setList(
//                            allEvents,
//                            userId
//                        )
//                    }
//                    mViewDataBinding.eventsLayout.visibility =
//                        if (allEvents.isNotEmpty()) {
//                            View.VISIBLE
//                        } else {
//                            View.GONE
//                        }
//                    eventAdapterIsSet = true
//                } else {
//                    mViewDataBinding.eventsLayout.visibility = View.GONE
//                }
//            }
        }
//        eventsAdapter.fileClickListener = { view: View, position: Int, data: EventFiles ->
//            val bundle = Bundle()
//            bundle.putParcelable("eventFile", data)
//            navigate(R.id.fileViewerFragment, bundle)
//        }
//
//
//        eventsAdapter.openEventImageClickListener =
//            { _: View, position: Int, imageFiles: List<TaskFiles> ->
////                viewModel.openImageViewer(requireContext(), fileUrls, position)
//                val bundle = Bundle()
//                bundle.putParcelableArray("images", imageFiles.toTypedArray())
//                bundle.putInt("position", position)
//                bundle.putBoolean("fromServerUrl", true)
//                navigate(R.id.imageViewerFragment, bundle)
//            }
    }


    private fun showTaskInfoBottomSheet() {
        val sheet = TaskInfoBottomSheet(
            _rootState = viewModel.rootState,
            _selectedState = viewModel.selectedState,
            _userId = viewModel.user?.id ?: "",
            _taskDetail = viewModel.taskDetail.value
        )
//        sheet.dialog?.window?.setSoftInputMode(
//            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or
//                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
//        );

        sheet.isCancelable = true
        sheet.show(childFragmentManager, "TaskInfoBottomSheet")
    }


    override fun onNavigationResult(result: BackNavigationResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            when (result.requestCode) {
                FORWARD_TASK_REQUEST_CODE -> {
                    val eventData = result.data?.getParcelable<EventV2Response.Data>("eventData")
                    if (eventData != null) {
                        val taskEvent = Events(
                            id = eventData.id,
                            taskId = eventData.taskId,
                            eventType = eventData.eventType,
                            initiator = eventData.initiator,
                            eventData = eventData.eventData,
                            commentData = eventData.commentData,
                            createdAt = eventData.createdAt,
                            updatedAt = eventData.updatedAt,
                            invitedMembers = eventData.invitedMembers,
                            eventNumber = eventData.eventNumber
                        )
                        addEventsToUI(taskEvent)
                    }
                    result.data?.clear()
                }

                COMMENT_REQUEST_CODE -> {
                    val eventData = result.data?.getParcelable<EventV2Response.Data>("eventData")
                    if (eventData != null) {
                        val taskEvent = Events(
                            id = eventData.id,
                            taskId = eventData.taskId,
                            eventType = eventData.eventType,
                            initiator = eventData.initiator,
                            eventData = eventData.eventData,
                            commentData = eventData.commentData,
                            createdAt = eventData.createdAt,
                            updatedAt = eventData.updatedAt,
                            invitedMembers = eventData.invitedMembers,
                            eventNumber = eventData.eventNumber
                        )
                        addEventsToUI(taskEvent)
                    }
                    result.data?.clear()
                }

                DONE_REQUEST_CODE -> {
                    val eventData = result.data?.getParcelable<EventV2Response.Data>("eventData")
                    val isBeingDone = result.data?.getBoolean("isBeingDone")
                    if (isBeingDone == true) {
                        shortToastNow("Marking task as done...")
                        mViewDataBinding.doneBtn.isEnabled = false
                        mViewDataBinding.doneBtn.isClickable = false
                        mViewDataBinding.doneBtn.alpha = 0.6f
                        mViewDataBinding.taskForwardBtn.isEnabled = false
                        mViewDataBinding.taskForwardBtn.isClickable = false
                        mViewDataBinding.taskForwardBtn.alpha = 0.6f
                    }
                    if (eventData != null) {
                        val taskEvent = Events(
                            id = eventData.id,
                            taskId = eventData.taskId,
                            eventType = eventData.eventType,
                            initiator = eventData.initiator,
                            eventData = eventData.eventData,
                            commentData = eventData.commentData,
                            createdAt = eventData.createdAt,
                            updatedAt = eventData.updatedAt,
                            invitedMembers = eventData.invitedMembers,
                            eventNumber = eventData.eventNumber
                        )
                        addEventsToUI(taskEvent)
                    }
                    result.data?.clear()
                }
            }
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
                        scrollToBottom(allEvents.size)
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
                    scrollToBottom(allEvents.size)
//                    val seenByMe = task?.seenBy?.find { it == viewModel.user?.id }
//                    if (seenByMe == null) {
//                        viewModel.taskSeen(taskEvent.taskId) { }
//                    }
                }
            }

        }
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
    fun onRefreshAllEvents(
        event: LocalEvents.RefreshAllEvents?
    ) {
        viewModel.getAllEventsFromLocalEvents()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskFailedToDone(event: LocalEvents.TaskFailedToDone?) {
        mViewDataBinding.doneBtn.isEnabled = true
        mViewDataBinding.doneBtn.isClickable = true
        mViewDataBinding.doneBtn.alpha = 1f
        mViewDataBinding.taskForwardBtn.isEnabled = true
        mViewDataBinding.taskForwardBtn.isClickable = true
        mViewDataBinding.taskForwardBtn.alpha = 1f
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaskSeenEvent(event: LocalEvents.TaskSeenEvent?) {
        val task = event?.task
        if (task != null) {
            viewModel.taskDetail.value?.let { taskDetail ->
                if (task.id == taskDetail.id) {
                    task.let { it1 ->
                        taskSeenRequest = true
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
                    taskSeenRequest = true
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
                                scrollToBottom(allEvents.size)
//                            }, 1)

                            }
                        } else {
                            val eventList = mutableListOf<Events>()
                            eventList.add(taskEvent)
                            allEvents.addAll(eventList)
                            viewModel.updateTaskAndAllEvents(taskEvent, allEvents)
                            scrollToBottom(allEvents.size)
                        }
                    }
                }
            }
        }
    }


    private fun scrollToBottom(size: Int) {
//        if (!isScrolling) {
//            isScrolling = true
        var scrollDelay: Long = 370
        if (size >= 40 && size < 52) {
            scrollDelay = 450
        } else if (size >= 52) {
            scrollDelay = 530
        }
        Handler(Looper.getMainLooper()).postDelayed({
            mViewDataBinding.bodyScroll.isFocusableInTouchMode = true
            mViewDataBinding.bodyScroll.descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
            mViewDataBinding.bodyScroll.requestLayout()
            mViewDataBinding.bodyScroll.scrollToBottomWithoutFocusChange()
//            mViewDataBinding.bodyScroll.fullScroll(View.FOCUS_DOWN)
//            isScrolling = false
        }, scrollDelay)
//        }
    }

    private fun scrollToBottomWithDelay() {
//        isScrollingWithDelay = true
//        mViewDataBinding.bodyScroll.postDelayed({
//            mViewDataBinding.bodyScroll.fullScroll(View.FOCUS_DOWN)
//        }, 400)
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
            }
        }

        /* Handler(Looper.getMainLooper()).postDelayed({
             getDownloadProgress(context, downloadId!!) {
                 itemClickListener?.invoke(it)
             }
         }, 1000)*/

        println("id: ${id} Folder name: ${folder} uri:${uri} destinationUri:${destinationUri}")

    }

    @SuppressLint("Range")
    private fun getDownloadProgress(
        context: Context?,
        downloadId: Long,
        itemClickListener: ((tag: String) -> Unit)?
    ) {
        GlobalScope.launch {
            while (true) {
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
                    }
                } else {
                    itemClickListener?.invoke("retry")
                    break
                }
                cursor.close()

                delay(500)
            }
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

            mimeType == "application/x-rar-compressed" || mimeType == "application/zip" -> {
                AttachmentTypes.Zip
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

    private fun openFile(file: File, context: Context) {

        val intent = Intent(context, All_Document_Reader_Activity::class.java)
        intent.putExtra("path", file.absolutePath)
        intent.putExtra("fromAppActivity", true)
        context.startActivity(intent)
        return

    }
}