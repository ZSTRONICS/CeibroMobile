package stkq.draw

data class FiveTuple<T, U, V, W, X>(
    val actualX: T,
    val actualY: U,
    val zoomLevel: V,
    val eventX: W,
    val eventY: X
)