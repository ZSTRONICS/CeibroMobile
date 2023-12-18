package com.zstronics.ceibro.ui.locationv2.usage

import androidx.annotation.Keep

@Keep
data class PinPointsData(
    val points: List<Point>
)

@Keep
data class Point(
    val type: String,
    val xPoint: Float,
    val yPoint: Float,
    val width: Int,
    val height: Int,
)