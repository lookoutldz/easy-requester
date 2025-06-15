package io.github.lookoutldz.easyrequester.requester.common

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
 *  @author looko
 *  @date 2025/6/6
 *
 */
abstract class AbstractEasyHttp internal constructor(
    protected val url: String,
    protected val params: Map<String, String>? = null,
    protected val headers: Map<String, String>? = null,
    protected val cookies: Map<String, String>? = null,
    protected val okHttpClient: OkHttpClient,
    protected val responseHandler: (Response) -> Unit,
    protected val exceptionHandler: (Throwable, Request) -> Unit
) {
    
    abstract class Builder<T>() {
        protected var okHttpClient: OkHttpClient? = null
        protected var objectMapper: ObjectMapper? = null
        
        protected lateinit var url: String
        protected var params: Map<String, String>? = null
        protected var headers: Map<String, String>? = null
        protected var cookies: Map<String, String>? = null
        protected var body: Any? = null
        protected var contentType: String? = null

        protected var clazz: Class<T>? = null
        protected var typeReference: TypeReference<T>? = null

        protected var responseHandler: ((Response) -> Unit)? = null
        protected var responseSuccessHandler: ((Response) -> Unit)? = null
        protected var responseFailureHandler: ((Response) -> Unit)? = null
        protected var successHandler: ((T?) -> Unit)? = null
        protected var exceptionHandler: ((Throwable, Request) -> Unit)? = null

        constructor(clazz: Class<T>) : this() { this.clazz = clazz }
        constructor(typeReference: TypeReference<T>) : this() { this.typeReference = typeReference }

        fun setOkHttpClient(okHttpClient: OkHttpClient?): Builder<T> = apply { this.okHttpClient = okHttpClient ?: OkHttpClient() }
        fun setObjectMapper(objectMapper: ObjectMapper?): Builder<T> = apply { this.objectMapper = objectMapper ?: specifiedObjectMapper }

        fun setUrl(url: String): Builder<T> = apply { this.url = url }
        fun setParams(params: Map<String, String>?): Builder<T> = apply { this.params = params }
        fun setHeaders(headers: Map<String, String>?): Builder<T> = apply { this.headers = headers }
        fun setCookies(cookies: Map<String, String>?): Builder<T> = apply { this.cookies = cookies }
        fun setBody(body: Any?): Builder<T> = apply { this.body = body }
        fun setContentType(contentType: String?): Builder<T> = apply { this.contentType = contentType }

        fun onResponse(handler: (Response) -> Unit): Builder<T> = apply { this.responseHandler = handler }
        fun onResponseSuccess(handler: (Response) -> Unit): Builder<T> = apply { this.responseSuccessHandler = handler }
        fun onResponseFailure(handler: (Response) -> Unit): Builder<T> = apply { this.responseFailureHandler = handler }
        fun onSuccess(handler: (T?) -> Unit): Builder<T> = apply { this.successHandler = handler }
        fun onException(handler: (Throwable, Request) -> Unit): Builder<T> = apply { this.exceptionHandler = handler }

        private val specifiedObjectMapper by lazy {
            getEffectiveObjectMapper(dataClassInClass(clazz) || dataClassInTypeReference(typeReference))
        }

        abstract fun build(): AbstractEasyHttp

        protected fun defaultResponseHandler(response: Response) {
            if (response.isSuccessful) {
                responseSuccessHandler?.invoke(response) ?: defaultResponseSuccessHandler(response)
            } else {
                responseFailureHandler?.invoke(response) ?: defaultResponseFailureHandler(response)
            }
        }

        @Suppress("UNCHECKED_CAST")
        protected fun defaultResponseSuccessHandler(response: Response) {
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

        protected fun defaultResponseFailureHandler(response: Response) {
            // 不读取body内容，避免资源泄漏和重复消费问题
            // 如果需要读取body，应该由用户在自定义handler中处理
            println("${response.code}-${response.message}: Response failed")
        }

        protected fun defaultSuccessHandler(t: T?) {
            println("SUCCESS: ${t.toString()}")
        }

        protected fun defaultExceptionHandler(e: Throwable, request: Request) {
            println("ERROR: [${request.method}]${request.url}: ${e.message}")
            throw e
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
        params: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        cookies: Map<String, String>? = null
    ): Request.Builder {

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
        // 若头中没有指定 User-Agent 则使用默认值
        if (headers?.any { it.key.equals(userAgentKey, true) } != true) {
            requestBuilder.addHeader(userAgentKey, userAgentValueDefault)
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


        return requestBuilder
    }
}