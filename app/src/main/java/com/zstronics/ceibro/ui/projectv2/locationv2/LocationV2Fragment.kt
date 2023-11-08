package com.zstronics.ceibro.ui.projectv2.locationv2


import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.LocationfragmentBinding
import com.zstronics.ceibro.ui.tasks.v2.hidden_tasks.adapter.HiddenRVAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationV2Fragment :
    BaseNavViewModelFragment<LocationfragmentBinding, ILocationV2.State, LocationV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: LocationV2VM by viewModels()
    override val layoutResId: Int = R.layout.locationfragment
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {

            R.id.cl_newProject -> {

            }
        }
    }


    @Inject
    lateinit var adapter: HiddenRVAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


}