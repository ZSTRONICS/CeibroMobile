package com.zstronics.ceibro.base.interfaces

import com.zstronics.ceibro.base.clickevents.SingleClickEvent

interface OnClickHandler {
    val clickEvent: SingleClickEvent?
    fun handlePressOnView(id: Int)
}