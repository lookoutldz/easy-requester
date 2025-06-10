package io.github.lookoutldz.easyrequester.requester4j

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.lookoutldz.easyrequester.requester4j.EasyHttpPost4j.Builder
import io.github.lookoutldz.easyrequester.requester4j.common.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * Java友好版本的DELETE请求类
 * @author looko
 * @date 2025/6/6
 */
class EasyHttpDelete4j<T> private constructor(
    url: String,
    params: Map<String, String>?,
    headers: Map<String, String>?,
    cookies: Map<String, String>?,
    okHttpClient: OkHttpClient,
    responseHandler: ResponseHandler?,
    exceptionHandler: ExceptionHandler?
) : AbstractEasyHttp4j<T>(
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

        override fun build(): EasyHttpDelete4j<T> {
            return EasyHttpDelete4j(
                url = url,
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
        val request = commonRequestGenerator(url, params, headers, cookies).delete().build()

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
}