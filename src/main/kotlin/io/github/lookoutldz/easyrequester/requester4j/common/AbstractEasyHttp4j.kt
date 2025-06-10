package io.github.lookoutldz.easyrequester.requester4j.common

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.lookoutldz.easyrequester.util.dataClassInClass
import io.github.lookoutldz.easyrequester.util.dataClassInTypeReference
import io.github.lookoutldz.easyrequester.util.getEffectiveObjectMapper
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * Java友好版本的抽象HTTP请求类
 * @author looko
 * @date 2025/6/6
 */
abstract class AbstractEasyHttp4j internal constructor(
    protected val url: String,
    protected val params: Map<String, String>?,
    protected val headers: Map<String, String>?,
    protected val cookies: Map<String, String>?,
    protected val okHttpClient: OkHttpClient,
    protected val responseHandler: ResponseHandler?,
    protected val exceptionHandler: ExceptionHandler?
) {

    abstract class Builder<T> {
        protected var okHttpClient: OkHttpClient? = null
        protected var objectMapper: ObjectMapper? = null
        
        protected var url: String = ""
        protected var params: Map<String, String>? = null
        protected var headers: Map<String, String>? = null
        protected var cookies: Map<String, String>? = null
        protected var body: Any? = null
        protected var contentType: String? = null

        protected var clazz: Class<T>? = null
        protected var typeReference: TypeReference<T>? = null

        protected var responseHandler: ResponseHandler? = null
        protected var responseSuccessHandler: ResponseSuccessHandler? = null
        protected var responseFailureHandler: ResponseFailureHandler? = null
        protected var successHandler: SuccessHandler<T>? = null
        protected var exceptionHandler: ExceptionHandler? = null

        constructor(clazz: Class<T>) { this.clazz = clazz }
        constructor(typeReference: TypeReference<T>) { this.typeReference = typeReference }

        fun setOkHttpClient(okHttpClient: OkHttpClient?): Builder<T> {
            this.okHttpClient = okHttpClient ?: OkHttpClient()
            return this
        }
        
        fun setObjectMapper(objectMapper: ObjectMapper?): Builder<T> {
            this.objectMapper = objectMapper ?: getSpecifiedObjectMapper()
            return this
        }

        fun setUrl(url: String): Builder<T> {
            this.url = url
            return this
        }
        
        fun setParams(params: Map<String, String>?): Builder<T> {
            this.params = params
            return this
        }
        
        fun setHeaders(headers: Map<String, String>?): Builder<T> {
            this.headers = headers
            return this
        }
        
        fun setCookies(cookies: Map<String, String>?): Builder<T> {
            this.cookies = cookies
            return this
        }
        
        fun setBody(body: Any?): Builder<T> {
            this.body = body
            return this
        }
        
        fun setContentType(contentType: String?): Builder<T> {
            this.contentType = contentType
            return this
        }

        fun onResponse(handler: ResponseHandler?): Builder<T> {
            this.responseHandler = handler
            return this
        }
        
        fun onResponseSuccess(handler: ResponseSuccessHandler?): Builder<T> {
            this.responseSuccessHandler = handler
            return this
        }
        
        fun onResponseFailure(handler: ResponseFailureHandler?): Builder<T> {
            this.responseFailureHandler = handler
            return this
        }
        
        fun onSuccess(handler: SuccessHandler<T>?): Builder<T> {
            this.successHandler = handler
            return this
        }
        
        fun onException(handler: ExceptionHandler?): Builder<T> {
            this.exceptionHandler = handler
            return this
        }

        private fun getSpecifiedObjectMapper(): ObjectMapper {
            return getEffectiveObjectMapper(dataClassInClass(clazz) || dataClassInTypeReference(typeReference))
        }

        abstract fun build(): AbstractEasyHttp4j

        protected fun getDefaultResponseHandler(): ResponseHandler {
            return object : ResponseHandler {
                override fun onResponse(response: Response) {
                    if (response.isSuccessful) {
                        responseSuccessHandler?.onResponseSuccess(response) ?: getDefaultResponseSuccessHandler().onResponseSuccess(response)
                    } else {
                        responseFailureHandler?.onResponseFailure(response) ?: getDefaultResponseFailureHandler().onResponseFailure(response)
                    }
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        protected fun getDefaultResponseSuccessHandler(): ResponseSuccessHandler {
            return object : ResponseSuccessHandler {
                override fun onResponseSuccess(response: Response) {
                    val result = response.body?.let { body ->
                        val mapper = objectMapper ?: getSpecifiedObjectMapper()
                        when {
                            clazz != null -> {
                                if (clazz == String::class.java) {
                                    body.string() as T
                                } else {
                                    mapper.readValue(body.byteStream(), clazz) as T
                                }
                            }
                            typeReference != null -> {
                                mapper.readValue(body.byteStream(), typeReference) as T
                            }
                            else -> throw RuntimeException("No Class or TypeReference Specified!")
                        }
                    }
                    successHandler?.onSuccess(result) ?: getDefaultSuccessHandler().onSuccess(result)
                }
            }
        }

        protected fun getDefaultResponseFailureHandler(): ResponseFailureHandler {
            return object : ResponseFailureHandler {
                override fun onResponseFailure(response: Response) {
                    // 不读取body内容，避免资源泄漏和重复消费问题
                    // 如果需要读取body，应该由用户在自定义handler中处理
                    println("${response.code}-${response.message}: Response failed")
                }
            }
        }

        protected fun getDefaultSuccessHandler(): SuccessHandler<T> {
            return object : SuccessHandler<T> {
                override fun onSuccess(result: T?) {
                    println("SUCCESS: ${result.toString()}")
                }
            }
        }

        protected fun getDefaultExceptionHandler(): ExceptionHandler {
            return object : ExceptionHandler {
                override fun onException(error: Throwable, request: Request) {
                    println("ERROR: [${request.method}]${request.url}: ${error.message}")
                    throw error
                }
            }
        }
    }

    abstract fun execute()

    private val userAgentKey = "User-Agent"
    private val userAgentValueDefault = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36"

    /**
     * 抽取公用方法
     */
    internal fun commonRequestGenerator(
        url: String,
        params: Map<String, String>?,
        headers: Map<String, String>?,
        cookies: Map<String, String>?
    ): Request.Builder {
        val requestBuilder = Request.Builder()

        // 参数处理
        if (params != null && params.isNotEmpty()) {
            val filteredParams = params.filterNot { (key, _) -> key.isBlank() }
            if (filteredParams.isNotEmpty()) {
                val urlBuilder = url.toHttpUrl().newBuilder()
                for ((key, value) in filteredParams) {
                    urlBuilder.addQueryParameter(key, value)
                }
                requestBuilder.url(urlBuilder.build())
            } else {
                requestBuilder.url(url)
            }
        } else {
            requestBuilder.url(url)
        }

        // 头信息处理
        if (headers != null && headers.isNotEmpty()) {
            val filteredHeaders = headers.filterNot { (key, value) -> key.isBlank() || value.isBlank() }
            for ((key, value) in filteredHeaders) {
                requestBuilder.header(key, value)
            }
        }
        
        // 若头中没有指定 User-Agent 则使用默认值
        val hasUserAgent = headers?.any { it.key.equals(userAgentKey, true) } ?: false
        if (!hasUserAgent) {
            requestBuilder.addHeader(userAgentKey, userAgentValueDefault)
        }

        // Cookie处理
        if (cookies != null && cookies.isNotEmpty()) {
            val filteredCookies = cookies.filterNot { (key, value) -> key.isBlank() || value.isBlank() }
            if (filteredCookies.isNotEmpty()) {
                val cookieString = filteredCookies.entries.joinToString("; ") { (key, value) -> "$key=$value" }
                if (cookieString.isNotBlank()) {
                    requestBuilder.addHeader("Cookie", cookieString)
                }
            }
        }

        return requestBuilder
    }
}