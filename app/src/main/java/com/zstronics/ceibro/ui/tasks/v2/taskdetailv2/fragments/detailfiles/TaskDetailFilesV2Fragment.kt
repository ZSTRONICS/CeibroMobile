package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailfiles

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentTaskDetailFilesV2Binding
import com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailfiles.adapter.TaskDetailFilesAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TaskDetailFilesV2Fragment :
    BaseNavViewModelFragment<FragmentTaskDetailFilesV2Binding, ITaskDetailFilesV2.State, TaskDetailFilesV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskDetailFilesV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_detail_files_v2
    override fun toolBarVisibility(): Boolean = false
    private val filesAdapterList = arrayOf("All", "Photos", "Links", "Files")
    private lateinit var eventsAdapter: TaskDetailFilesAdapter
    override fun onClick(id: Int) {
        when (id) {
            R.id.tvAll -> {
                changeTaBackgroundColor(1)
            }

            R.id.tvPhotos -> {
                changeTaBackgroundColor(2)

            }

            R.id.tvLinks -> {
                changeTaBackgroundColor(3)

            }

            R.id.tvDocuments -> {
                changeTaBackgroundColor(4)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, filesAdapterList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mViewDataBinding.filesSpinner.adapter = adapter


        mViewDataBinding.filesSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    shortToastNow(filesAdapterList[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }

        eventsAdapter = TaskDetailFilesAdapter(
            networkConnectivityObserver,
            requireContext(),
            viewModel.downloadedDrawingV2Dao
        )
        mViewDataBinding.filesRV.adapter = eventsAdapter

    }

    private fun changeTaBackgroundColor(index: Int) {
        val selectedBackground = R.drawable.signin_button_back
        val selectedTint = R.color.appBlue
        val selectedTextColor = R.color.white

        val unselectedBackground = R.drawable.signin_button_back
        val unselectedTint = R.color.appGrey1
        val unselectedTextColor = R.color.appGrey3

        when (index) {
            1 -> {

                applyBackgroundTintTextColors(
                    mViewDataBinding.tvAll,
                    selectedBackground,
                    selectedTint,
                    selectedTextColor
                )

                applyBackgroundTintTextColors(
                    mViewDataBinding.tvPhotos,
                    selectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvLinks,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvDocuments,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )

            }

            2 -> {
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvAll,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvPhotos,
                    selectedBackground,
                    selectedTint,
                    selectedTextColor
                )
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvLinks,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvDocuments,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )

            }

            3 -> {
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvAll,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvPhotos,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )

                applyBackgroundTintTextColors(
                    mViewDataBinding.tvLinks,
                    selectedBackground,
                    selectedTint,
                    selectedTextColor
                )
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvDocuments,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )
            }

            4 -> {
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvAll,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvPhotos,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )
                applyBackgroundTintTextColors(
                    mViewDataBinding.tvLinks,
                    unselectedBackground,
                    unselectedTint,
                    unselectedTextColor
                )

                applyBackgroundTintTextColors(
                    mViewDataBinding.tvDocuments,
                    selectedBackground,
                    selectedTint,
                    selectedTextColor
                )
            }
        }
    }

    private fun applyBackgroundTintTextColors(
        textView: TextView,
        backgroundRes: Int,
        tintRes: Int,
        textRes: Int
    ) {
        textView.setBackgroundResource(backgroundRes)
        textView.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), tintRes)
        )
        textView.setTextColor(ContextCompat.getColor(requireContext(), textRes))
    }

}