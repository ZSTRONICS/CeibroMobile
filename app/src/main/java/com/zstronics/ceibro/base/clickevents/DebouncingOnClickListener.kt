package com.zstronics.ceibro.base.clickevents

import android.view.View
import android.widget.Toast

class DebouncingOnClickListener(
    private val intervalMillis: Long,
    private val doClick: ((View) -> Unit)
) : View.OnClickListener {
    private var enabled = true

    override fun onClick(v: View) {
        if (enabled) {
            enabled = false
            v.postDelayed2(intervalMillis) { enabled = true }
            doClick(v)
        }
    }

    private fun View.postDelayed2(delayMillis: Long, action: () -> Unit) =
        postDelayed(action, delayMillis)
}

fun View.setOnClick(intervalMillis: Long = 0, doClick: (View) -> Unit) =
    setOnClickListener(
        DebouncingOnClickListener(
            intervalMillis = intervalMillis,
            doClick = doClick
        )
    )

fun testDebouncingOnClickListener(v: View) {
    v.setOnClick {
        Toast.makeText(v.context, "No double click any more! ", Toast.LENGTH_SHORT).show()
    }
    v.setOnClick(500) {
        Toast.makeText(v.context, "No double click any more! ", Toast.LENGTH_SHORT).show()
    }
}