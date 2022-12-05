package com.zstronics.ceibro.base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.databinding.ProgressDialogueFragmentBinding

class ProgressDialogueFragment : DialogFragment() {
    lateinit var binding: ProgressDialogueFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ProgressDialogueFragmentBinding.inflate(
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
        binding.circularProgressBar.visibility = View.VISIBLE
        binding.layer.visibility = View.VISIBLE
        binding.layer.alpha = 0f
        binding.layer.animate().alpha(0.6f).setDuration(300)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    binding.circularProgressBar.indeterminateMode = true
                    binding.layer.visibility = View.VISIBLE
                }
            })
    }

    override fun onDismiss(dialog: DialogInterface) {
        binding.circularProgressBar.clearProgressAnimation()
        binding.circularProgressBar.visibility = View.GONE
        binding.layer.visibility = View.GONE
        super.onDismiss(dialog)
    }
}