package io.github.lookoutldz.easyrequester.requester.common

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.lookoutldz.easyrequester.entity.HttpMethod
import io.github.lookoutldz.easyrequester.requester.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * HTTP请求通用工具类，消除各个HTTP方法类中伴生对象的重复代码
 * @author looko
 * @date 2025/6/6
 */
object EasyHttpRequestHelper {

    /**
     * 通用的带类型参数的请求方法
     */
    inline fun <reified T> doRequest(
        method: HttpMethod,
        url: String,
        body: Any? = null,
        contentType: String? = "application/json",
        params: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        cookies: Map<String, String>? = null,
        okHttpClient: OkHttpClient? = null,
        objectMapper: ObjectMapper? = null,
        noinline responseHandler: ((Response) -> Unit)? = null,
        noinline responseSuccessHandler: ((response: Response) -> Unit)? = null,
        noinline responseFailureHandler: ((response: Response) -> Unit)? = null,
        noinline exceptionHandler: ((error: Throwable?, request: Request) -> Unit)? = null,
        noinline successHandler: ((t: T?) -> Unit)? = null,
    ) {
        val builder = when (method) {
            HttpMethod.GET -> EasyHttpGet.Builder(T::class.java)
            HttpMethod.POST -> EasyHttpPost.Builder(T::class.java)
                .setBody(body)
                .setContentType(contentType)
            HttpMethod.PUT -> EasyHttpPut.Builder(T::class.java)
                .setBody(body)
                .setContentType(contentType)
            HttpMethod.DELETE -> EasyHttpDelete.Builder(T::class.java)
        }

        builder
            .setUrl(url)
            .setParams(params)
            .setHeaders(headers)
            .setCookies(cookies)
            .setOkHttpClient(okHttpClient)
            .setObjectMapper(objectMapper)
            .apply {
                responseHandler?.let { onResponse(it) }
                responseSuccessHandler?.let { onResponseSuccess(it) }
                responseFailureHandler?.let { onResponseFailure(it) }
                exceptionHandler?.let { onException(it) }
                successHandler?.let { onSuccess(it) }
            }
            .build()
            .execute()
    }

    /**
     * 默认String类型的请求方法
     */
    fun doRequestDefault(
        method: HttpMethod,
        url: String,
        body: Any? = null,
        contentType: String = "application/json",
        params: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        cookies: Map<String, String>? = null,
        okHttpClient: OkHttpClient? = null,
        objectMapper: ObjectMapper? = null,
        responseSuccessHandler: ((response: Response) -> Unit)? = null,
        responseFailureHandler: ((response: Response) -> Unit)? = null,
        exceptionHandler: ((error: Throwable?, request: Request) -> Unit)? = null,
        successHandler: ((t: String?) -> Unit)? = null,
    ) = doRequest<String>(
        method = method,
        url = url,
        body = body,
        contentType = contentType,
        params = params,
        headers = headers,
        cookies = cookies,
        okHttpClient = okHttpClient,
        objectMapper = objectMapper,
        responseSuccessHandler = responseSuccessHandler,
        responseFailureHandler = responseFailureHandler,
        exceptionHandler = exceptionHandler,
        successHandler = successHandler,
    )

    /**
     * 原始返回处理器，用户可以自行处理返回体
     */
    fun doRequestRaw(
        method: HttpMethod,
        url: String,
        body: Any? = null,
        contentType: String? = null,
        params: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        cookies: Map<String, String>? = null,
        okHttpClient: OkHttpClient? = null,
        objectMapper: ObjectMapper? = null,
        exceptionHandler: ((Throwable?, Request) -> Unit)? = null,
        responseHandler: ((Response) -> Unit)? = null,
    ) = doRequest<Any>(
        method = method,
        url = url,
        body = body,
        contentType = contentType,
        params = params,
        headers = headers,
        cookies = cookies,
        okHttpClient = okHttpClient,
        objectMapper = objectMapper,
        exceptionHandler = exceptionHandler,
        responseHandler = responseHandler,
    )

}
