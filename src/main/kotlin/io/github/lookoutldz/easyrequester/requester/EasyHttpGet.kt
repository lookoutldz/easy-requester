package io.github.lookoutldz.easyrequester.requester

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.lookoutldz.easyrequester.util.isDataClass
import io.github.lookoutldz.easyrequester.util.isKotlinModuleRegistered
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import kotlin.String

class EasyHttpGet<T> private constructor(
    private val url: String,
    private val params: Map<String, String>? = null,
    private val headers: Map<String, String>? = null,
    private val cookies: Map<String, String>? = null,
    private val okHttpClient: OkHttpClient,
    private val responseHandler: (Response) -> Unit,
    private val exceptionHandler: (Throwable, Request) -> Unit
) {

    // 添加一个伴生对象，提供便捷的创建方法
    companion object {
        // 使用 reified 类型参数创建便捷方法
        inline fun <reified T> doRequest(
            url: String,
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
            return Builder(T::class.java)
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

        // 创建默认的 String 类型处理器
        fun doRequestDefault(
            url: String,
            params: Map<String, String>? = null,
            headers: Map<String, String>? = null,
            cookies: Map<String, String>? = null,
            okHttpClient: OkHttpClient? = null,
            objectMapper: ObjectMapper? = null,
            responseSuccessHandler: ((response: Response) -> Unit)? = null,
            responseFailureHandler: ((response: Response) -> Unit)? = null,
            exceptionHandler: ((error: Throwable?, request: Request) -> Unit)? = null,
            successHandler: ((t: String?) -> Unit)? = null,
        ) {
            return doRequest<String>(
                url = url,
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
        }

        // 创建原始Response处理器
        fun doRequestRaw(
            url: String,
            params: Map<String, String>? = null,
            headers: Map<String, String>? = null,
            cookies: Map<String, String>? = null,
            okHttpClient: OkHttpClient? = null,
            objectMapper: ObjectMapper? = null,
            exceptionHandler: ((Throwable?, Request) -> Unit)? = null,
            responseHandler: ((Response) -> Unit)? = null,
        ) {
            return doRequest<Any>(
                url = url,
                params = params,
                headers = headers,
                cookies = cookies,
                okHttpClient = okHttpClient,
                objectMapper = objectMapper,
                exceptionHandler = exceptionHandler,
                responseHandler = responseHandler,
            )
        }
    }

    class Builder<T>() {

        private var okHttpClient: OkHttpClient? = null
        private var objectMapper: ObjectMapper? = null

        private lateinit var url: String
        private var params: Map<String, String>? = null
        private var headers: Map<String, String>? = null
        private var cookies: Map<String, String>? = null

        private var clazz: Class<T>? = null
        private var typeReference: TypeReference<T>? = null

        private var responseHandler: ((Response) -> Unit)? = null
        private var responseSuccessHandler: ((Response) -> Unit)? = null
        private var responseFailureHandler: ((Response) -> Unit)? = null
        private var successHandler: ((T?) -> Unit)? = null
        private var exceptionHandler: ((Throwable, Request) -> Unit)? = null

        constructor(clazz: Class<T>) : this() { this.clazz = clazz }
        constructor(typeReference: TypeReference<T>) : this() { this.typeReference = typeReference }

        fun setOkHttpClient(okHttpClient: OkHttpClient?): Builder<T> = apply { this.okHttpClient = okHttpClient ?: OkHttpClient() }
        fun setObjectMapper(objectMapper: ObjectMapper?): Builder<T> = apply { this.objectMapper = objectMapper ?: specifiedObjectMapper }

        fun setUrl(url: String): Builder<T> = apply { this.url = url }
        fun setParams(params: Map<String, String>?): Builder<T> = apply { this.params = params }
        fun setHeaders(headers: Map<String, String>?): Builder<T> = apply { this.headers = headers }
        fun setCookies(cookies: Map<String, String>?): Builder<T> = apply { this.cookies = cookies }

        fun onResponse(handler: (Response) -> Unit): Builder<T> = apply { this.responseHandler = handler }
        fun onResponseSuccess(handler: (Response) -> Unit): Builder<T> = apply { this.responseSuccessHandler = handler }
        fun onResponseFailure(handler: (Response) -> Unit): Builder<T> = apply { this.responseFailureHandler = handler }
        fun onSuccess(handler: (T?) -> Unit): Builder<T> = apply { this.successHandler = handler }
        fun onException(handler: (Throwable, Request) -> Unit): Builder<T> = apply { this.exceptionHandler = handler }

        private val specifiedObjectMapper by lazy {
            if (isDataClass(clazz) || isDataClass(typeReference)) {
                return@lazy objectMapper ?: ObjectMapper().registerKotlinModule()
            } else {
                return@lazy objectMapper ?: ObjectMapper()
            }
        }

        fun build(): EasyHttpGet<T> {
            return EasyHttpGet(
                url = url,
                params = params,
                headers = headers,
                cookies = cookies,
                okHttpClient = okHttpClient ?: OkHttpClient(),
                responseHandler = responseHandler ?: this::defaultResponseHandler,
                exceptionHandler = exceptionHandler ?: this::defaultExceptionHandler
            )
        }

        private fun defaultResponseHandler(response: Response) {
            if (response.isSuccessful) {
                responseSuccessHandler?.invoke(response) ?: defaultResponseSuccessHandler(response)
            } else {
                responseFailureHandler?.invoke(response) ?: defaultResponseFailureHandler(response)
            }
        }

        @Suppress("UNCHECKED_CAST")
        private fun defaultResponseSuccessHandler(response: Response) {
            val t = response.body?.let { body ->
                val objectMapper = this.objectMapper ?: specifiedObjectMapper
                if (clazz != null) {
                    if (clazz == String::class.java) {
                        body.string() as T
                    } else {
                        objectMapper.readValue(body.byteStream(), clazz) as T
                    }
                } else if (typeReference != null) {
                    objectMapper.readValue(body.byteStream(), typeReference) as T
                } else {
                    throw RuntimeException("No Class or TypeReference Specified!")
                }
            }

            successHandler?.invoke(t) ?: defaultSuccessHandler(t)
        }

        private fun defaultResponseFailureHandler(response: Response) {
            println("${response.code}-${response.message}: ${response.body?.string()}")
        }

        private fun defaultSuccessHandler(t: T?) {
            println("SUCCESS: ${t.toString()}")
        }

        private fun defaultExceptionHandler(e: Throwable, request: Request) {
            println("ERROR: [${request.method}]${request.url}: ${e.message}")
            throw e
        }

    }

    fun execute() {
        // 构建请求
        val request = generateRequest(url, params, headers, cookies)

        try {
            // 发起请求
            okHttpClient.newCall(request).execute().use { response ->
                // 使用 use 安全管理资源
                responseHandler.invoke(response)
            }

        } catch (e: Exception) {
            exceptionHandler.invoke(e, request)
        }
    }

    private val userAgentKey = "User-Agent"
    private val userAgentValueDefault = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36"

    private fun generateRequest(
        url: String,
        params: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        cookies: Map<String, String>? = null
    ): Request {

        val requestBuilder = Request.Builder()

        // 参数处理
        params
            ?.filterNot { (key, _) ->
                key.isBlank()
            }
            ?.let {
                val urlBuilder = url.toHttpUrl().newBuilder()
                for ((key, value) in it) {
                    urlBuilder.addQueryParameter(key, value)
                }
                requestBuilder.url(urlBuilder.build())
            }
            ?: requestBuilder.url(url)

        // 头信息处理
        headers
            ?.filterNot { (key, value) ->
                key.isBlank() || value.isBlank()
            }
            ?.forEach { (key, value) ->
                requestBuilder.header(key, value)
            }

        // Cookie处理
        cookies
            ?.filterNot { (key, value) ->
                key.isBlank() || value.isBlank()
            }
            ?.let { cookieMap ->
                val cookieString = cookieMap.entries.joinToString("; ") { (key, value) -> "$key=$value" }
                if (cookieString.isNotBlank()) {
                    requestBuilder.addHeader("Cookie", cookieString)
                }
            }

        // 若没有指定 User-Agent 则使用默认值
        if (headers?.any { it.key.equals(userAgentKey, true) } == false) {
            requestBuilder.addHeader(userAgentKey, userAgentValueDefault)
        }

        return requestBuilder.build()
    }

}