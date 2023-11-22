package com.zstronics.ceibro.base.navgraph

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import android.transition.ChangeBounds
import android.transition.Fade
import android.transition.Slide
import android.view.Menu
import android.view.MenuInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.os.bundleOf
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import androidx.work.*
import com.ceibro.permissionx.PermissionX
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.BaseBindingViewModelFragment
import com.zstronics.ceibro.base.extensions.launchActivity
import com.zstronics.ceibro.base.extensions.launchActivityForResult
import com.zstronics.ceibro.base.extensions.toast
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.base.interfaces.ManageToolBarListener
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.remote.TaskRemoteDataSource
import com.zstronics.ceibro.data.repos.task.models.v2.SocketReSyncV2Response
import com.zstronics.ceibro.extensions.openFilePicker
import com.zstronics.ceibro.ui.attachment.AttachmentTypes
import com.zstronics.ceibro.ui.attachment.SubtaskAttachment
import com.zstronics.ceibro.ui.contacts.ContactSyncWorker
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.socket.SocketHandler
import com.zstronics.ceibro.utils.FileUtils
import ee.zstronics.photoediting.EditImageActivity
import okhttp3.internal.immutableListOf
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

private const val ARGUMENT_NAVIGATION_REQUEST_CODE = "NAVIGATION_REQUEST_CODE"

const val DESTINATION_NOT_SET = -1
const val REQUEST_CODE_NOT_SET = -1

const val NAVIGATION_RESULT_CANCELED = 0
const val NAVIGATION_RESULT_OK = -1

abstract class BaseNavViewModelFragment<VB : ViewDataBinding, VS : IBase.State, VM : HiltBaseViewModel<VS>> :
    BaseBindingViewModelFragment<VB, VS, VM>() {
    private val REQUEST_IMAGE_CAPTURE: Int = 1122
    private var currentPhotoPath: String = ""
    private val PERMISSION_REQUEST_EXTERNAL_STORAGE = 1
    protected open val hasUpNavigation: Boolean = true
    private val requestCode: Int
        get() = arguments?.getInt(ARGUMENT_NAVIGATION_REQUEST_CODE, REQUEST_CODE_NOT_SET)
            ?: REQUEST_CODE_NOT_SET

    override fun postExecutePendingBindings(savedInstanceState: Bundle?) {
        super.postExecutePendingBindings(savedInstanceState)
        if (activity is ManageToolBarListener) {
            (activity as ManageToolBarListener).toolBarTitle = getToolBarTitle()
            (activity as ManageToolBarListener).toolBarVisibility = toolBarVisibility()
            (activity as ManageToolBarListener).displayHomeAsUpEnabled = setDisplayHomeAsUpEnabled()
            (activity as ManageToolBarListener).homeAsUpIndicator = setHomeAsUpIndicator()
        }
    }

    override fun getToolBarTitle(): String? = null
    fun setToolBarTitle(title: String?) {
        if (activity is ManageToolBarListener) {
            (activity as ManageToolBarListener).toolBarTitle = title
        }
    }

    override fun toolBarVisibility(): Boolean? = true
    override fun setDisplayHomeAsUpEnabled(): Boolean? = true
    override fun setHomeAsUpIndicator() = R.drawable.icon_back


    /**
     * Navigates to the specified destination screen.
     *
     * @param destinationId the id of the destination screen (either the new Activity or Fragment)
     * @param extras the extra arguments to be passed to the destination screen
     * @param navigationExtras
     */
    fun navigate(
        @IdRes destinationId: Int,
        extras: Bundle? = Bundle(),
        navigationExtras: Navigator.Extras? = null,
        navOptions: NavOptions? = navOptions {
            anim {
                enter = R.anim.slide_in_right
                exit = R.anim.slide_out_left
                popEnter = R.anim.slide_in_left
                popExit = R.anim.slide_out_right
            }
        }
    ) {
        getFragmentResult()?.apply {
            val (key, bundle) = this
            key?.let {
                setFragmentResult(it, bundle ?: bundleOf())
            }
        }

        findNavController().navigate(
            destinationId,
            extras,
            navOptions,
            navigationExtras,
        )
    }

    /**
     * Navigates to the specified destination screen.
     *
     * @param directions the direction that leads to the destiantion screen.
     * @param navigationExtras
     */
    protected fun navigate(directions: NavDirections, navigationExtras: Navigator.Extras? = null) {
        navigationExtras?.let { navExtras ->
            findNavController().navigate(directions, navExtras)
        } ?: run {
            findNavController().navigate(directions)
        }
    }

    fun navigateWithPopup(
        @IdRes destinationId: Int, @IdRes popupTo: Int, extras: Bundle? = Bundle(),
        navigationExtras: Navigator.Extras? = null, enableAnimation: Boolean = true
    ) {
        navigate(
            destinationId,
            extras,
            navigationExtras,
            navOptions = navOptions {
                popUpTo(popupTo) {
                    inclusive = true
                }
                if (enableAnimation) {
                    anim {
                        enter = R.anim.slide_in_right
                        exit = R.anim.slide_out_left
                        popEnter = R.anim.slide_in_left
                        popExit = R.anim.slide_out_right
                    }
                }
            })
    }

    /**
     * Navigates back (pops the back stack) to the previous [MvvmFragment] on the stack.
     */
    protected fun navigateBack() {
        findNavController().popBackStack()
    }

    protected fun navigateUp(): Boolean = findNavController().navigateUp()

    protected fun navigateForResultWithAnimation(
        requestCode: Int, navDirections: NavDirections, navOptions: NavOptions? = navOptions {
            anim {
                enter = R.anim.slide_in_right
                exit = R.anim.slide_out_left
                popEnter = R.anim.slide_in_left
                popExit = R.anim.slide_out_right
            }
        }
    ) {
        // val extras = FragmentNavigatorExtras(appBarLayout to appBarTransition)
        this.exitTransition = Fade()
        navigateForResult(
            resId = navDirections.actionId,
            requestCode = requestCode,
            args = navDirections.arguments,
            navOptions = navOptions,
            navigatorExtras = null
        )
    }

    protected fun navigateForResult(
        requestCode: Int, navDirections: NavDirections, navOptions: NavOptions? = navOptions {
            anim {
                enter = R.anim.slide_in_right
                exit = R.anim.slide_out_left
                popEnter = R.anim.slide_in_left
                popExit = R.anim.slide_out_right
            }
        }, extras: Bundle? = Bundle(),

        navigatorExtras: Navigator.Extras? = null
    ) =
        navigateForResult(
            resId = navDirections.actionId,
            requestCode = requestCode,
            args = extras,
            navOptions = navOptions,
            navigatorExtras = navigatorExtras,

            )

    protected fun navigateForResult(
        @IdRes resId: Int,
        requestCode: Int,
        args: Bundle? = null,
        navOptions: NavOptions? = navOptions {
            anim {
                enter = R.anim.slide_in_right
                exit = R.anim.slide_out_left
                popEnter = R.anim.slide_in_left
                popExit = R.anim.slide_out_right
            }
        },
        navigatorExtras: Navigator.Extras? = null
    ) {
        val argsWithRequestCode = (args ?: Bundle()).apply {
            putInt(ARGUMENT_NAVIGATION_REQUEST_CODE, requestCode)
        }
        navigatorExtras?.let {
            findNavController().navigate(
                resId,
                argsWithRequestCode,
                navOptions,
                navigatorExtras
            )
        } ?: findNavController().navigate(resId, argsWithRequestCode, navOptions)
    }

    protected fun navigateForwardWithAnimation(navDirections: NavDirections) {
        // val extras = FragmentNavigatorExtras(appBarLayout to appBarTransition)
        this.exitTransition = Slide()
        findNavController().navigate(navDirections)
    }

    protected fun navigateForwardWithAnimation(
        navDirections: NavDirections,
        args: Bundle?,
        exitTransition: Any? = Slide()
    ) {
        // val extras = FragmentNavigatorExtras(appBarLayout to appBarTransition)
        // exitTransition?.let { this.exitTransition = it }
//        this.enterTransition = Slide(Gravity.RIGHT)
        navigateForResult(navDirections.actionId, REQUEST_CODE_NOT_SET, args)
    }

    protected fun navigate(
        navDirections: NavDirections,
        args: Bundle?
    ) {
        navigateForResult(navDirections.actionId, REQUEST_CODE_NOT_SET, args)
    }

    protected fun navigateBackWithResult(resultCode: Int, data: Bundle? = null): Boolean =
        navigateBackWithResult(
            DESTINATION_NOT_SET,
            BackNavigationResult(requestCode, resultCode, data)
        )

    protected fun navigateBackWithResult(
        @IdRes destination: Int,
        resultCode: Int,
        data: Bundle? = null
    ): Boolean =
        navigateBackWithResult(destination, BackNavigationResult(requestCode, resultCode, data))

    protected fun initEnterTransitions() {
        sharedElementEnterTransition = ChangeBounds()
        enterTransition = Fade()
    }

    private fun navigateBackWithResult(
        @IdRes destination: Int,
        result: BackNavigationResult
    ): Boolean {
        val childFragmentManager =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.childFragmentManager
        var backStackListener: FragmentManager.OnBackStackChangedListener by Delegates.notNull()
        backStackListener = FragmentManager.OnBackStackChangedListener {
            (childFragmentManager?.fragments?.get(0) as? BackNavigationResultListener)?.onNavigationResult(
                result
            )
            childFragmentManager?.removeOnBackStackChangedListener(backStackListener)
        }
        childFragmentManager?.addOnBackStackChangedListener(backStackListener)
        val backStackPopped = if (destination == DESTINATION_NOT_SET) {
            findNavController().popBackStack()
        } else {
            findNavController().popBackStack(destination, true)
        }
        if (!backStackPopped) {
            childFragmentManager?.removeOnBackStackChangedListener(backStackListener)
        }
        return backStackPopped
    }

    fun checkPermission(permissionsList: List<String>, function: () -> Unit) {
        PermissionX.init(this).permissions(
            permissionsList
        )
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    function.invoke()
                } else {
                    //toast(getString(R.string.common_text_permissions_denied))
                }
            }
    }

    fun isPermissionGranted(permission: String): Boolean {
        return PermissionX.isGranted(context, permission)
    }

    fun goToPermissionSettings(permission: String, permissionsList: List<String>) {
        PermissionX.init(this).permissions(permission).forwardToSettings(permissionsList)
    }

    fun pickAttachment(allowMultiple: Boolean = false) {
        checkPermission(
            immutableListOf(
                Manifest.permission.CAMERA,
            )
        ) {
            pickFiles(allowMultiple)
        }
    }

    fun chooseImage(onPhotoPick: (fileUri: Uri?) -> Unit) {
        checkPermission(
            immutableListOf(
                Manifest.permission.CAMERA,
            )
        ) {
            requireActivity().openFilePicker(
                allowMultiple = false,
                mimeTypes = arrayOf(
                    "image/png",
                    "image/jpg",
                    "image/jpeg",
                    "image/*"
                )
            ) { resultCode, data ->
                if (resultCode == Activity.RESULT_OK && data != null) {
                    onPhotoPick(data.data)
                }
            }
        }
    }


    fun handleSocketReSyncDataEvent() {
        SocketHandler.getSocket()?.on(SocketHandler.CEIBRO_RE_SYNC_DATA) { args ->
            val gson = Gson()
            val arguments = args[0].toString()
            val reSyncData = gson.fromJson<SocketReSyncV2Response>(
                arguments,
                object : TypeToken<SocketReSyncV2Response>() {}.type
            ).data
            println("Heartbeat, handleSocketReSyncDataEvent: ${arguments}")

            viewModel.loading(true, "Syncing App Data")
            Handler(Looper.getMainLooper()).postDelayed({

                viewModel.launch {
                    viewModel.reSyncAppData(reSyncData) { isSuccess ->
                        viewModel.loading(false, "")
                        SocketHandler.sendClearData()
                    }
                }

            }, 500)
        }
    }



//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//        if (!Environment.isExternalStorageManager()) {
//            requestManageAllFilesAccessPermission()
//            return
//        } else {
//            pickFiles()
//        }
//    } else {
//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.READ_EXTERNAL_STORAGE
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            pickFiles()
//            return
//        } else {
//            requestPermissions(
//                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
//                PERMISSION_REQUEST_EXTERNAL_STORAGE
//            )
//            return
//        }

    //converting -> content://com.android.providers.media.documents/document/image%3A17     into next file format so that name would be accurate in all devices    file:///storage/emulated/0/Android/data/com.zstronics.ceibro.dev/files/1695738659642.jpg
    fun createFileUriFromContentUri(context: Context, contentUri: Uri): Uri? {
        val outputPath = context.getExternalFilesDir(null)?.absolutePath
        val filename = System.currentTimeMillis().toString() + ".jpg"

        try {
            val input = context.contentResolver.openInputStream(contentUri)
            val destinationFile = File(outputPath, filename)
            val output = FileOutputStream(destinationFile)

            input?.use { input ->
                output.use { output ->
                    input.copyTo(output)
                }
            }

            return Uri.fromFile(destinationFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return contentUri
    }

    private fun pickFiles(allowMultiple: Boolean = true) {
        requireActivity().openFilePicker(
            allowMultiple = allowMultiple,
            mimeTypes = arrayOf(
                "image/png",
                "image/jpg",
                "image/jpeg",
                "image/*",
                "video/mp4",
                "video/3gpp",
                "video/*",
                "application/pdf",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

            )
        ) { resultCode, data ->
            if (resultCode == Activity.RESULT_OK && data != null) {
                val clipData = data.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val fileUri = clipData.getItemAt(i).uri
                        // Add the URI to the list
                        addFileToUriList(fileUri)
                    }
                } else {
                    val fileUri = data.data
                    // Add the URI to the list
                    addFileToUriList(fileUri)
                }
            }
        }
    }

    fun captureAttachment() {
        checkPermission(
            immutableListOf(
                Manifest.permission.CAMERA
            )
        ) {
            takePhoto()
        }
    }

    fun createNotification(
        channelId: String?,
        chanelName: String,
        notificationTitle: String = "Uploading file",
        isOngoing: Boolean = true,
        indeterminate: Boolean = false,
        notificationIcon: Int = R.drawable.icon_upload
    ): Pair<NotificationManager, NotificationCompat.Builder> {
        // Create a notification channel (for Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                requireActivity().getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                channelId,
                chanelName,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
        // Create a notification builder
        val builder = NotificationCompat.Builder(requireContext(), channelId ?: "channel_id")
            .setSmallIcon(notificationIcon)
            .setContentTitle(notificationTitle)
            .setOngoing(isOngoing)
            .setOnlyAlertOnce(true)
        if (isOngoing) {
            builder.setProgress(100, 1, indeterminate)
        }
        // Show the notification
        val notificationManager =
            requireActivity().getSystemService(NotificationManager::class.java)
        notificationManager.notify(channelId.hashCode(), builder.build())
        return Pair(notificationManager, builder)
    }

    private fun addFileToUriList(fileUri: Uri?) {
        val mimeType = FileUtils.getMimeType(requireContext(), fileUri)
        val fileName = FileUtils.getFileName(requireContext(), fileUri)
        val fileSize = FileUtils.getFileSizeInBytes(requireContext(), fileUri)
        val fileSizeReadAble = FileUtils.getReadableFileSize(fileSize)
        val attachmentType = when {
            mimeType == null -> {
                AttachmentTypes.Doc
            }

            mimeType.startsWith("image") -> {
                AttachmentTypes.Image
            }

            mimeType.startsWith("video") -> {
                AttachmentTypes.Video
            }

            mimeType == "application/pdf" -> {
                AttachmentTypes.Pdf
            }

            mimeType == "application/msword" || mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> {
                AttachmentTypes.Doc
            }

            else -> AttachmentTypes.Doc
        }
        viewModel.addUriToList(
            SubtaskAttachment(
                attachmentType,
                fileUri,
                fileSize,
                fileSizeReadAble,
                fileName
            )
        )
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_add, menu)
    }


    fun startEditor(imageUri: Uri, onPhotoEditedCallback: (updatedUri: Uri?) -> Unit) {
        launchActivityForResult<EditImageActivity>(init = {
            this.data = imageUri
            action = Intent.ACTION_EDIT
        }) { resultCode, data ->
            onPhotoEditedCallback(data?.data)

            if (imageUri.toString().contains("content://media/")) {
                try {
                    val contentResolver = requireContext().contentResolver
                    val projection = arrayOf(MediaStore.Images.Media.DATA)
                    val cursor = contentResolver.query(imageUri, projection, null, null, null)
                    val filePath: String? = cursor?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                        } else {
                            null
                        }
                    }
                    val fileToDelete = filePath?.let { File(it) }
                    if (fileToDelete?.exists() == true) {
                        val deleted = fileToDelete.delete()
                    }
                } catch (_: Exception) {
                }
            } else {
                try {
                    val oldFile = imageUri.toFile()
                    if (oldFile.exists()) {
                        val deleted = oldFile.delete()
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val permission = Environment.isExternalStorageManager()
                    if (permission) {
                        pickFiles()
                    } else {
                        toast(getString(R.string.common_text_permissions_denied))
                    }
                }
            } else {
                toast(getString(R.string.common_text_permissions_denied))
            }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestManageAllFilesAccessPermission() {
        val intent = Intent(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.data = Uri.parse("package:" + requireActivity().packageName)
        requestPermissionLauncher.launch(intent)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickFiles()
            } else {
                toast(getString(R.string.common_text_permissions_denied))
            }
        }
    }

    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            // Create the file where the photo should go
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                null
            }

            // Continue only if the file was successfully created
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.zstronics.ceibro.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir: File? = context?.cacheDir
        return File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // Image captured and saved to fileUri specified in the Intent
            // Do something with the saved image file
            val photoFile = File(currentPhotoPath)
            val savedUri = Uri.fromFile(photoFile)
//            addFileToUriList(savedUri)
            startEditor(savedUri) { updatedUri ->
                if (updatedUri != null) {
                    addFileToUriList(updatedUri)
                }
            }
        }
    }

    fun startPeriodicContactSyncWorker(context: Context) {
        checkPermission(
            immutableListOf(
                Manifest.permission.READ_CONTACTS,
            )
        ) {
            // Build the constraints
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicWorkRequest = PeriodicWorkRequest.Builder(
                ContactSyncWorker::class.java, 15, TimeUnit.MINUTES
            ).setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                ContactSyncWorker.CONTACT_SYNC_WORKER_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
        }
    }

    fun startOneTimeContactSyncWorker(context: Context) {
        checkPermission(
            immutableListOf(
                Manifest.permission.READ_CONTACTS,
            )
        ) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val customBackoffDelayMillis =
                60 * 60 * 1000L // Set the initial delay to 1 hour (adjust as needed)

            val oneTimeWorkRequest = OneTimeWorkRequest.Builder(ContactSyncWorker::class.java)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    customBackoffDelayMillis,
                    TimeUnit.MILLISECONDS
                )
                .build()

            println("PhoneNumber-OneTimeContactSyncWorker")
            WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)
            if (!isNetworkConnected(context)) {
                // Network is not connected, fire the event
                EventBus.getDefault().post(LocalEvents.UpdateConnections)
            }
        }
    }

    // Helper function to check network connectivity
    private fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun navigateToAppLoadingScreen() {
        launchActivity<NavHostPresenterActivity>(
            options = Bundle(),
            clearPrevious = true
        ) {
            putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
            putExtra(
                NAVIGATION_Graph_START_DESTINATION_ID,
                R.id.ceibroDataLoadingFragment
            )
        }
    }


    fun isUserNameValid(name: String): Boolean {
        val regex =
            Regex("^[\\p{L}][\\p{L}0-9\\s]*\$") // "\p{L}" Allow alphabetical characters from various languages, making it a suitable pattern to accept all alphabet characters while excluding special characters and spaces
        return regex.matches(name)
    }

    fun startsWithAlphabet(name: String): Boolean {
        val regex = Regex("^[\\p{L}].*")
        return name.isNotEmpty() && regex.matches(name)
    }
}