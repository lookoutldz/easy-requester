package io.github.lookoutldz.easyrequester.requester4j

import com.fasterxml.jackson.core.type.TypeReference
import io.github.lookoutldz.easyrequester.requester4j.common.*
import io.github.lookoutldz.easyrequester.util.dataClassInClass
import io.github.lookoutldz.easyrequester.util.getEffectiveObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Java友好版本的POST请求类
 * @author looko
 * @date 2025/6/6
 */
class EasyHttpPost4j<T> private constructor(
    url: String,
    params: Map<String, String>?,
    headers: Map<String, String>?,
    cookies: Map<String, String>?,
    okHttpClient: OkHttpClient,
    responseHandler: ResponseHandler?,
    exceptionHandler: ExceptionHandler?,
    private val body: Any?,
    private val contentType: String
) : AbstractEasyHttp4j(
    url = url,
    params = params,
    headers = headers,
    cookies = cookies,
    okHttpClient = okHttpClient,
    responseHandler = responseHandler,
    exceptionHandler = exceptionHandler
) {

    companion object {
        /**
         * 创建带 clazz 的处理器
         */
        @JvmStatic
        fun <T> doRequest(
            clazz: Class<T>,
            url: String,
            body: Any?,
            contentType: String?,
            successHandler: SuccessHandler<T>
        ) =
            Builder(clazz)
                .setUrl(url)
                .setBody(body)
                .setContentType(contentType)
                .onSuccess(successHandler)
                .build()
                .execute()

        @JvmStatic
        fun <T> doRequest(
            clazz: Class<T>,
            url: String,
            body: Any?,
            contentType: String?,
            successHandler: SuccessHandler<T>,
            exceptionHandler: ExceptionHandler
        ) =
            Builder(clazz)
                .setUrl(url)
                .setBody(body)
                .setContentType(contentType)
                .onSuccess(successHandler)
                .onException(exceptionHandler)
                .build()
                .execute()

        /**
         * 创建带 typeReference 的处理器
         */
        @JvmStatic
        fun <T> doRequest(
            typeReference: TypeReference<T>,
            url: String,
            body: Any?,
            contentType: String?,
            successHandler: SuccessHandler<T>
        ) =
            Builder(typeReference)
                .setUrl(url)
                .setBody(body)
                .setContentType(contentType)
                .onSuccess(successHandler)
                .build()
                .execute()

        @JvmStatic
        fun <T> doRequest(
            typeReference: TypeReference<T>,
            url: String,
            body: Any?,
            contentType: String?,
            successHandler: SuccessHandler<T>,
            exceptionHandler: ExceptionHandler
        ) =
            Builder(typeReference)
                .setUrl(url)
                .setBody(body)
                .setContentType(contentType)
                .onSuccess(successHandler)
                .onException(exceptionHandler)
                .build()
                .execute()

        /**
         * 创建默认的 String 类型处理器
         */
        @JvmStatic
        fun doRequestDefault(
            url: String,
            body: Any?,
            contentType: String?,
            successHandler: SuccessHandler<String>?
        ) =
            Builder(String::class.java)
                .setUrl(url)
                .setBody(body)
                .setContentType(contentType)
                .onSuccess(successHandler)
                .build()
                .execute()

        @JvmStatic
        fun doRequestDefault(
            url: String,
            body: Any?,
            contentType: String?,
            successHandler: SuccessHandler<String>,
            exceptionHandler: ExceptionHandler
        ) =
            Builder(String::class.java)
                .setUrl(url)
                .setBody(body)
                .setContentType(contentType)
                .onSuccess(successHandler)
                .onException(exceptionHandler)
                .build()
                .execute()

        /**
         * 创建原始返回处理器, 用户可以自行处理返回体
         */
        @JvmStatic
        fun doRequestRaw(
            url: String,
            body: Any?,
            contentType: String?,
            responseHandler: ResponseHandler
        ) =
            Builder(Object::class.java)
                .setUrl(url)
                .setBody(body)
                .setContentType(contentType)
                .onResponse(responseHandler)
                .build()
                .execute()

        @JvmStatic
        fun doRequestRaw(
            url: String,
            body: Any?,
            contentType: String?,
            responseHandler: ResponseHandler,
            exceptionHandler: ExceptionHandler
        ) =
            Builder(Object::class.java)
                .setUrl(url)
                .setBody(body)
                .setContentType(contentType)
                .onResponse(responseHandler)
                .onException(exceptionHandler)
                .build()
                .execute()

    }

    class Builder<T> : AbstractEasyHttp4j.Builder<T> {
        constructor(clazz: Class<T>) : super(clazz)
        constructor(typeReference: TypeReference<T>) : super(typeReference)

        override fun build(): EasyHttpPost4j<T> {
            return EasyHttpPost4j(
                url = url,
                body = body,
                contentType = contentType ?: "application/json",
                params = params,
                headers = headers,
                cookies = cookies,
                okHttpClient = okHttpClient ?: OkHttpClient(),
                responseHandler = responseHandler ?: getDefaultResponseHandler(),
                exceptionHandler = exceptionHandler ?: getDefaultExceptionHandler()
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
                responseHandler?.onResponse(response)
            }
        } catch (e: Exception) {
            exceptionHandler?.onException(e, request)
        }
    }

    private val contentTypeKey = "Content-Type"

    private fun generateRequest(
        url: String,
        params: Map<String, String>?,
        headers: Map<String, String>?,
        cookies: Map<String, String>?,
        body: Any?,
        contentType: String
    ): Request {
        val mergedHeaders = headers?.toMutableMap() ?: mutableMapOf()
        // 设置Content-Type
        val hasContentType = mergedHeaders.any { it.key.equals(contentTypeKey, true) }
        if (!hasContentType) {
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