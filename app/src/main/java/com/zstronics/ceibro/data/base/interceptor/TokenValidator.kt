package com.zstronics.ceibro.data.base.interceptor


internal interface TokenValidator {
    var tokenRefreshInProgress: Boolean
    fun invalidate() {}
}