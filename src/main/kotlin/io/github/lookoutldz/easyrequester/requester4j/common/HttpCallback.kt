package io.github.lookoutldz.easyrequester.requester4j.common

import okhttp3.Request
import okhttp3.Response

/**
 * HTTP请求回调接口，Java友好版本
 * @author looko
 * @date 2025/6/6
 */
interface ResponseHandler {
    fun onResponse(response: Response)
}

interface ResponseSuccessHandler {
    fun onResponseSuccess(response: Response)
}

interface ResponseFailureHandler {
    fun onResponseFailure(response: Response)
}

interface SuccessHandler<T> {
    fun onSuccess(result: T?)
}

interface ExceptionHandler {
    fun onException(error: Throwable, request: Request)
}