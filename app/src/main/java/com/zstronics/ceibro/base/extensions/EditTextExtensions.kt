package com.zstronics.ceibro.base.extensions

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.zstronics.ceibro.R


fun TextInputEditText.setupClearButtonWithAction() {

    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            val clearIcon = if (editable?.isNotEmpty() == true) R.drawable.icon_close_round_grey else 0
            setCompoundDrawablesWithIntrinsicBounds(0, 0, clearIcon, 0)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    })

    setOnTouchListener(View.OnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            if (event.rawX >= (this.right - this.compoundPaddingRight)) {
                this.setText("")
                return@OnTouchListener true
            }
        }
        return@OnTouchListener false
    })

}