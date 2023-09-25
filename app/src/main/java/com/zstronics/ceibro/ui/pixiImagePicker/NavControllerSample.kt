package com.zstronics.ceibro.ui.pixiImagePicker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.databinding.FragmentPixiImagePickerBinding
import io.ak1.pix.helpers.PixBus
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.setupScreen

@Deprecated("New library integrated for image pick and camera capture, named as ImagePicker")
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
            when (it.status) {
                PixEventCallback.Status.SUCCESS -> {
                    val intent = Intent()
                    intent.data = it.data[0]
                    setResult(PHOTO_PICK_RESULT_CODE, intent)
                    finish()
                }
                PixEventCallback.Status.BACK_PRESSED -> {
                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    companion object {
        const val PHOTO_PICK_RESULT_CODE = 9876
    }
}