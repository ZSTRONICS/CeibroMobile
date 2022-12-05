package com.zstronics.ceibro.data.base

import retrofit2.Response

internal interface INetwork {
    suspend fun <T : BaseResponse> executeSafely(call: suspend () -> Response<T>): ApiResponse<T>
}