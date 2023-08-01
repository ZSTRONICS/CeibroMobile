package com.zstronics.ceibro.base

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.databinding.CeibroLogoDialogueFragmentBinding

class ProgressDialogueFragment : DialogFragment() {
    lateinit var binding: CeibroLogoDialogueFragmentBinding
    var count = 0
    lateinit var handler: Handler
    lateinit var runnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CeibroLogoDialogueFragmentBinding.inflate(
            layoutInflater,
            container,
            false
        )
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomDialog)
        isCancelable = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startSplashAnimation()
    }


    private fun startSplashAnimation() {
        // Set the total duration for the animation (5 seconds)
        val totalDuration = 5000L

        // The duration for each individual animation
        val individualDuration = 740L

        // Calculate the number of times to run the animation
        val repeatCount = totalDuration / (individualDuration * 2)

        object : CountDownTimer(totalDuration, individualDuration * 2) {
            override fun onTick(millisUntilFinished: Long) {
                // Run the animation on each tick
                animateLogo()
            }

            override fun onFinish() {
                // Animation finished
                // You can navigate to the next screen or perform any other action here.
            }
        }.start()
    }

    private fun animateLogo() {
        // Run the scaleX animation
        binding.centerLogoC.animate()
            .scaleX(-1F)
            .setDuration(370)
            .withEndAction {
                // After the first animation is finished, run the second scaleX animation
                binding.centerLogoC.animate()
                    .scaleX(1F).duration = 370
            }
    }
}