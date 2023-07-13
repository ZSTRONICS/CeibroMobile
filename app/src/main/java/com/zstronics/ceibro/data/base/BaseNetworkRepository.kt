package com.zstronics.ceibro.data.base

import com.zstronics.ceibro.data.base.error.ApiError
import org.json.JSONObject
import retrofit2.Response
import com.google.gson.stream.MalformedJsonException as MalformedJsonException1

const val MALFORMED_JSON_EXCEPTION_CODE = 0

abstract class BaseNetworkRepository : INetwork {

    override suspend fun <T : BaseResponse> executeSafely(call: suspend () -> Response<T>): ApiResponse<T> {
        try {
            val response: Response<T> = call.invoke()
            if (response.isSuccessful) {
                return ApiResponse.Success(response.code(), response.body()!!)
            }

            return ApiResponse.Error(detectError(response))

        } catch (exception: MalformedJsonException1) {
            return ApiResponse.Error(
                ApiError(
                    MALFORMED_JSON_EXCEPTION_CODE,
                    "No response from server"
                )
            )
        } catch (exception: Exception) {
            if (exception.localizedMessage?.contains("Unable to resolve host") == true) {
                return ApiResponse.Error(
                    ApiError(
                        getDefaultCode(), "Host error or No internet"
                    )
                )
            }
            else {
                return ApiResponse.Error(
                    ApiError(
                        getDefaultCode(),
                        "No response from server"
                    )
                )
            }
        }
    }

    private fun <T : BaseResponse> detectError(response: Response<T>): ApiError {
        val jsonObj = JSONObject(response.errorBody()!!.charStream().readText())
        return when (response.code()) {
            401 -> getApiError(mapError(NetworkErrors.Unauthorized, response.code(), jsonObj.getString("message")))
            403 -> getApiError(mapError(NetworkErrors.Forbidden, response.code(), response.message()))
            404 -> getApiError(mapError(NetworkErrors.NotFound, response.code(), response.message()))
            502 -> getApiError(mapError(NetworkErrors.BadGateway, response.code(), "No response from server"))
            504 -> getApiError(mapError(NetworkErrors.NoInternet, response.code(), response.message()))
            in 400..500 -> getApiError(mapError(NetworkErrors.InternalServerError, response.code(), jsonObj.getString("message")))
            -1009 -> getApiError(mapError(NetworkErrors.NoInternet, response.code(), response.message()))
            -1001 -> getApiError(mapError(NetworkErrors.RequestTimedOut, response.code(), response.message()))
            else -> {
                getApiError(mapError(NetworkErrors.UnknownError(), response.code(), response.message()))
            }
        }
    }


    private fun getApiError(error: ServerError): ApiError {
        return ApiError(
            error.code ?: getDefaultCode(),
            error.message ?: getDefaultMessage()
        )
    }

    private fun mapError(error: NetworkErrors, code: Int = 0, message: String?): ServerError {
        return when (error) {

            is NetworkErrors.NoInternet -> ServerError(
                code,
                "It seems you're offline. Please try to reconnect and refresh to continue"
            )
            is NetworkErrors.RequestTimedOut -> ServerError(
                code,
                "It seems you're offline. Please try to reconnect and refresh to continue"
            )
            is NetworkErrors.BadGateway -> ServerError(code, "Bad Gateway")
            is NetworkErrors.NotFound -> ServerError(code, "Not Found")
            is NetworkErrors.Forbidden -> ServerError(
                code,
                "You don't have access to this information"
            )
            is NetworkErrors.InternalServerError -> ServerError(code, message)
            is NetworkErrors.UnknownError -> ServerError(code, getDefaultMessage())
            is NetworkErrors.Unauthorized -> ServerError(code, message)
        }
    }

    private fun getDefaultMessage(): String {
        return "Something went wrong."
    }

    private fun getDefaultCode(): Int {
        return 0
    }


    data class ServerError(val code: Int?, val message: String?)

    sealed class NetworkErrors {
        object NoInternet : NetworkErrors()
        object RequestTimedOut : NetworkErrors()
        object BadGateway : NetworkErrors()
        object NotFound : NetworkErrors()
        object Forbidden : NetworkErrors()
        object InternalServerError : NetworkErrors()
        open class UnknownError : NetworkErrors()
        object Unauthorized : NetworkErrors()
    }
}