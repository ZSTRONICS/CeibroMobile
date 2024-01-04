package stkq.draw

data class FourTuple<T, U, V, W>(
    val xPoint: T,
    val yPoint: U,
    val isNewPin: V,
    val pinData: W
)