package com.zstronics.ceibro.ui.projects.newproject.overview

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentProjectOverviewBinding
import com.zstronics.ceibro.extensions.openFilePicker
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.immutableListOf
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ProjectOverviewFragment :
    BaseNavViewModelFragment<FragmentProjectOverviewBinding, IProjectOverview.State, ProjectOverviewVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ProjectOverviewVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_project_overview
    override fun toolBarVisibility(): Boolean = true
    var cal: Calendar = Calendar.getInstance()

    private val dueDateSetListener =
        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd-MM-yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.US)

            val formatToSend = "dd-MM-yyyy"
            val sdf1 = SimpleDateFormat(formatToSend, Locale.US)

            viewState.dueDate.value = sdf1.format(cal.time)
        }

    override fun onClick(id: Int) {
        when (id) {
            R.id.projectPhoto -> {
                checkPermission(
                    immutableListOf(
                        Manifest.permission.CAMERA,
                    )
                ) {
                    chooseFile(
                        arrayOf(
                            "image/png",
                            "image/jpg",
                            "image/jpeg",
                            "image/*"
                        )
                    )
                }
            }
            R.id.dueDateText -> {
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
        }
    }

    private fun chooseFile(mimeTypes: Array<String>) {
        requireActivity().openFilePicker(
            getString(R.string.add_project_photo), mimeTypes,
            completionHandler = fileCompletionHandler
        )
    }

    private var fileCompletionHandler: ((resultCode: Int, data: Intent?) -> Unit)? = { _, intent ->
        intent?.let { intentData ->

            intentData.dataString?.let {
                viewState.projectPhoto.value = it
            }
        }
    }
}