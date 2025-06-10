package io.github.lookoutldz.easyrequester.requester

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.lookoutldz.easyrequester.requester.common.AbstractEasyHttp
import io.github.lookoutldz.easyrequester.util.dataClassInClass
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

/**
 *  @author looko
 *  @date 2025/5/21
 *  POST请求核心类
 */
class EasyHttpPost<T> private constructor(
    url: String,
    params: Map<String, String>? = null,
    headers: Map<String, String>? = null,
    cookies: Map<String, String>? = null,
    okHttpClient: OkHttpClient,
    responseHandler: (Response) -> Unit,
    exceptionHandler: (Throwable, Request) -> Unit,
    private val body: Any? = null,
    private val contentType: String = "application/json",
): AbstractEasyHttp<T>(
    url = url,
    params = params,
    headers = headers,
    cookies = cookies,
    okHttpClient = okHttpClient,
    responseHandler = responseHandler,
    exceptionHandler = exceptionHandler
) {

    // 添加一个伴生对象，提供便捷的创建方法
    companion object {

        /**
         * 使用了 reified 类型参数, 创建带类型的处理器
         * 使用方式:
         *  doRequest<T>(url) { t -> // do business }
         */
        inline fun <reified T> doRequest(
            url: String,
            body: Any? = null,
            contentType: String = "application/json",
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
        ) = Builder(T::class.java)
            .setUrl(url)
            .setBody(body)
            .setContentType(contentType)
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


        /**
         * 创建默认的 String 类型处理器
         * 使用方式:
         *  doRequestDefault(url) { string -> // do business }
         */
        fun doRequestDefault(
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
            successHandler = successHandler
        )

        /**
         * 创建原始返回处理器, 用户可以自行处理返回体
         * 使用方式:
         *  doRequestRaw(url) { response -> // do business }
         */
        fun doRequestRaw(
            url: String,
            body: Any? = null,
            contentType: String = "application/json",
            params: Map<String, String>? = null,
            headers: Map<String, String>? = null,
            cookies: Map<String, String>? = null,
            okHttpClient: OkHttpClient? = null,
            objectMapper: ObjectMapper? = null,
            exceptionHandler: ((Throwable?, Request) -> Unit)? = null,
            responseHandler: ((Response) -> Unit)? = null,
        ) = doRequest<Any>(
            url = url,
            body = body,
            contentType = contentType,
            params = params,
            headers = headers,
            cookies = cookies,
            okHttpClient = okHttpClient,
            objectMapper = objectMapper,
            exceptionHandler = exceptionHandler,
            responseHandler = responseHandler
        )
    }

    class Builder<T>: AbstractEasyHttp.Builder<T> {

        constructor(clazz: Class<T>) : super(clazz)
        constructor(typeReference: TypeReference<T>) : super(typeReference)

        override fun build(): EasyHttpPost<T> {
            return EasyHttpPost(
                url = url,
                body = body,
                contentType = contentType ?: "application/json",
                params = params,
                headers = headers,
                cookies = cookies,
                okHttpClient = okHttpClient ?: OkHttpClient(),
                responseHandler = responseHandler ?: this::defaultResponseHandler,
                exceptionHandler = exceptionHandler ?: this::defaultExceptionHandler
            )
        }

    }

    override fun execute() {
        // 构建请求
        val request = generateRequest(url, params, headers, cookies, body, contentType)

        try {
            // 发起请求
            okHttpClient.newCall(request).execute().use { response ->
                // 使用 use 安全管理资源
                responseHandler(response)
            }

        } catch (e: Exception) {
            exceptionHandler(e, request)
        }
    }

    private val contentTypeKey = "Content-Type"

    private fun generateRequest(
        url: String,
        params: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        cookies: Map<String, String>? = null,
        body: Any?,
        contentType: String,
    ): Request {

        val mergedHeaders = headers?.toMutableMap() ?: mutableMapOf()
        // 设置Content-Type
        if (!mergedHeaders.any { it.key.equals(contentTypeKey, true) }) {
            mergedHeaders[contentTypeKey] = contentType
        }

        val requestBuilder = commonRequestGenerator(url, params, mergedHeaders, cookies)

        // 处理请求体
        val requestBody = when (body) {
            is String -> body.toRequestBody(contentType.toMediaType())
            is ByteArray -> body.toRequestBody(contentType.toMediaType())
            is okhttp3.RequestBody -> body  // 直接使用传入的 RequestBody（包括 MultipartBody）
            null -> "".toRequestBody(contentType.toMediaType())
            else -> {
                val objectMapper = getEffectiveObjectMapper(dataClassInClass(body::class.java))
                objectMapper.writeValueAsString(body).toRequestBody(contentType.toMediaType())
            }
        }

        // 设置POST方法和请求体
        requestBuilder.post(requestBody)

        return requestBuilder.build()
    }
}