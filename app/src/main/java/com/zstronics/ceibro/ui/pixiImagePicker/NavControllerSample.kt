package com.zstronics.ceibro.ui.pixiImagePicker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.databinding.FragmentPixiImagePickerBinding
import io.ak1.pix.helpers.PixBus
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.setupScreen

class NavControllerSample : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var binding: FragmentPixiImagePickerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentPixiImagePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        setupScreen()
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        PixBus.results {
            if (it.status == PixEventCallback.Status.SUCCESS) {
                onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        if (navController.currentDestination == navController.graph.findNode(R.id.CameraFragment)) {
            PixBus.onBackPressedEvent()
        } else {
            super.onBackPressed()
        }
    }
}