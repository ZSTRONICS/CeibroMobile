package com.zstronics.ceibro.base.navgraph

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.Fade
import android.transition.Slide
import androidx.annotation.IdRes
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.ceibro.permissionx.PermissionX
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.BaseBindingViewModelFragment
import com.zstronics.ceibro.base.extensions.toast
import com.zstronics.ceibro.base.interfaces.IBase
import com.zstronics.ceibro.base.interfaces.ManageToolBarListener
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.extensions.openCamera
import com.zstronics.ceibro.extensions.openFilePicker
import com.zstronics.ceibro.ui.attachment.AttachmentTypes
import com.zstronics.ceibro.ui.attachment.SubtaskAttachment
import com.zstronics.ceibro.utils.FileUtils
import okhttp3.internal.immutableListOf
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

private const val ARGUMENT_NAVIGATION_REQUEST_CODE = "NAVIGATION_REQUEST_CODE"

const val DESTINATION_NOT_SET = -1
const val REQUEST_CODE_NOT_SET = -1

const val NAVIGATION_RESULT_CANCELED = 0
const val NAVIGATION_RESULT_OK = -1

abstract class BaseNavViewModelFragment<VB : ViewDataBinding, VS : IBase.State, VM : HiltBaseViewModel<VS>> :
    BaseBindingViewModelFragment<VB, VS, VM>() {
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
        ).explainReasonBeforeRequest().onExplainRequestReason { scope, deniedList, beforeRequest ->
            if (beforeRequest)
                scope.showRequestReasonDialog(
                    deniedList,
                    "${getString(R.string.common_text_permission)}",
                    getString(R.string.common_text_allow),
                    getString(R.string.common_text_deny)
                )
        }.onForwardToSettings { scope, deniedList ->
            scope.showForwardToSettingsDialog(
                permissions = deniedList,
                message = getString(R.string.message_camera_permission_denied),
                positiveText = getString(R.string.open_setting), cancelAble = true
            )
        }
            .request { allGranted, grantedList, deniedList ->
                if (allGranted) {
                    function.invoke()
                } else {
                    toast(getString(R.string.common_text_permissions_denied))
                }
            }
    }

    fun pickAttachment(allowMultiple: Boolean = false) {
        checkPermission(
            immutableListOf(
                Manifest.permission.CAMERA,
            )
        ) {
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
    }

    fun captureAttachment() {
        checkPermission(
            immutableListOf(
                Manifest.permission.CAMERA
            )
        ) {
            requireActivity().openCamera { resultCode, intent ->
                try {
                    val bitmap: Bitmap = intent?.extras?.get("data") as Bitmap
                    // Create a File object to save the bitmap
                    val timeStamp: String = java.lang.String.valueOf(
                        TimeUnit.MILLISECONDS.toSeconds(
                            System.currentTimeMillis()
                        )
                    )
                    val file = File(context?.cacheDir, "IMG-$timeStamp.jpg")
                    file.createNewFile()

                    val byteArrayOutputStream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                    val byteArray = byteArrayOutputStream.toByteArray()

                    val fileOutputStream = FileOutputStream(file)
                    fileOutputStream.write(byteArray)
                    fileOutputStream.flush()
                    fileOutputStream.close()
                    val uri = Uri.fromFile(file)
                    addFileToUriList(uri)
                } catch (e: Exception) {
                    toast(e.message.toString())
                }
            }
        }
    }

    private fun addFileToUriList(fileUri: Uri?) {
        val mimeType = FileUtils.getMimeType(requireContext(), fileUri)
        val fileName = FileUtils.getFileName(requireContext(), fileUri)
        val fileSize = FileUtils.getFileSizeInBytes(requireContext(), fileUri)
        val fileSizeReadAble = FileUtils.getReadableFileSize(fileSize)
        val attachmentType = when {
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
}