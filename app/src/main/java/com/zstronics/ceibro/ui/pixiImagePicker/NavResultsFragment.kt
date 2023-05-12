package com.zstronics.ceibro.ui.pixiImagePicker

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.zstronics.ceibro.R
import io.ak1.pix.helpers.PixBus
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.models.Flash
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import io.ak1.pix.models.Ratio
import io.ak1.pix.utility.ARG_PARAM_PIX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class NavResultsFragment : Fragment() {
    val options = Options().apply {
        ratio = Ratio.RATIO_AUTO                                    //Image/video capture ratio
        count =
            1                                                   //Number of images to restrict selection count
        spanCount = 4                                               //Number for columns in grid
        path =
            "Pix/Camera"                                         //Custom Path For media Storage
        isFrontFacing =
            false                                       //Front Facing camera on start
        mode =
            Mode.Picture                                             //Option to select only pictures or videos or both
        flash =
            Flash.Auto                                          //Option to select flash type
        preSelectedUrls = ArrayList()                          //Pre selected Image Urls
    }

    private val recyclerViewAdapter = Adapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PixBus.results(coroutineScope = CoroutineScope(Dispatchers.Main)) {
            when (it.status) {
                PixEventCallback.Status.SUCCESS -> {
                    recyclerViewAdapter.apply {
                        this.list.clear()
                        this.list.addAll(it.data)
                        notifyDataSetChanged()
                    }
                }
                PixEventCallback.Status.BACK_PRESSED -> {
                    requireActivity().onBackPressed()
                }
            }
        }

        var bundle = bundleOf(ARG_PARAM_PIX to options)

        findNavController().navigate(R.id.action_ResultsFragment_to_CameraFragment, bundle)
    }

}
