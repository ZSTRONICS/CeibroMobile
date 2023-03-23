package com.zstronics.ceibro.ui.projects.newproject.members.memberprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.databinding.FragmentProjectFilterBinding

class FilterProjectsSheet constructor(
    var statusesList: List<String>,
    var ownersList: List<AllProjectsResponse.Projects.Owner>,
) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentProjectFilterBinding
    var onFilter: ((ownerId: String, status: String) -> Unit)? = null
    var onClearFilter: (() -> Unit)? = null
    var ownerId = ""
    var status = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_project_filter,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /// Owners spinner
        val ownersStrings = ownersList.map { "${it.firstName} ${it.surName}" }
        val ownersArrayAdapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                ownersStrings
            )

        ownersArrayAdapter.setDropDownViewResource(
            android.R.layout
                .simple_spinner_dropdown_item
        )

        binding.ownerSpinner.setAdapter(ownersArrayAdapter)
        binding.ownerSpinner.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                ownerId = ownersList[position].id
            }

        /// End Owners spinner

        /// Status spinner
        val statusStrings = statusesList
        val statusArrayAdapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                statusStrings
            )

        statusArrayAdapter.setDropDownViewResource(
            android.R.layout
                .simple_spinner_dropdown_item
        )

        binding.statusSpinner.setAdapter(statusArrayAdapter)
        binding.statusSpinner.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                status = statusStrings[position]
            }
        /// End Status spinner

        binding.closeBtn.setOnClickListener {
            dismiss()
        }
        binding.clearFilter.setOnClickListener {
            onClearFilter?.invoke()
            dismiss()
        }

        binding.filterButton.setOnClick {
            if (status.isNotEmpty() || ownerId.isNotEmpty()) {
                onFilter?.invoke(ownerId, status)
                dismiss()
            } else {
                showToast("Filter required")
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }
}