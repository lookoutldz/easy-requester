package io.github.lookoutldz.easyrequester

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.lookoutldz.easyrequester.requester.EasyHttpHead
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.Test

/**
 *  @author looko
 *  @date 2025/6/6
 *  HEAD请求测试类
 */
class EasyHttpHeadRequesterTest {

    @Test
    fun contextLoads() {
        println("head context ok")
    }

    // HEAD请求通常用于获取资源的元信息，不返回响应体
    private val baseUrl = "http://127.0.0.1:58080/api/head"
    private val userHeadUrl = "http://127.0.0.1:58080/api/head/user/123"
    private val fileHeadUrl = "http://127.0.0.1:58080/api/head/file/document.pdf"
    private val params = mapOf("check" to "metadata")
    private val headers = mapOf("Accept" to "*/*")
    private val cookies = mapOf("sessionId" to "abc123")

    @Test
    fun testDoRequestDefault() {
        // 测试最简单的默认HEAD请求方式
        EasyHttpHead.doRequestDefault(url = userHeadUrl) {
            // HEAD请求通常不返回响应体，但可能返回空字符串
            println("01. HEAD ok - response body length: ${it?.length ?: 0}")
        }
    }

    @Test
    fun testDoRequestWithType() {
        // 测试指定返回类型的HEAD请求
        EasyHttpHead.doRequest<String>(url = userHeadUrl) { responseBody ->
            println("02. HEAD ok - ${responseBody}")
        }
    }

    @Test
    fun testDoRequestWithAllParams() {
        // 测试带有完整参数的HEAD请求
        EasyHttpHead.doRequest<ResponseBody<*>>(
            url = baseUrl,
            params = params,
            headers = headers,
            cookies = cookies,
            okHttpClient = OkHttpClient(),
            objectMapper = ObjectMapper().registerKotlinModule(),
        ) { responseBody ->
            println("03. HEAD ok - ${responseBody?.data}")
        }
    }

    @Test
    fun testDoRequestWithExceptionHandler() {
        // 测试自定义异常处理器
        EasyHttpHead.doRequest<ResponseBody<*>>(
            url = userHeadUrl,
            objectMapper = ObjectMapper(),  // Exception here: this is not the correct ObjectMapper for Kotlin Data Class
            exceptionHandler = { throwable: Throwable?, request: Request ->
                println("04. My ExceptionHandler - [${request.method}]${request.url} cause ${throwable?.message}")
            }
        ) { responseBody ->
            println("04. HEAD ok - ${responseBody?.data}")
        }
    }

    @Test
    fun testDoRequestRaw() {
        // 测试处理原始响应 - HEAD请求的主要用途
        EasyHttpHead.doRequestRaw(url = userHeadUrl) { response ->
            println("05. HEAD ok - ${response.isSuccessful} - ${response.code} - ${response.message}")
            println("    Content-Length: ${response.header("Content-Length")}")
            println("    Content-Type: ${response.header("Content-Type")}")
            println("    Last-Modified: ${response.header("Last-Modified")}")
            println("    ETag: ${response.header("ETag")}")
        }
    }

    @Test
    fun testBuilderPattern() {
        // 测试使用Builder模式构建HEAD请求
        EasyHttpHead
            .Builder(ResponseBody::class.java)
            .setUrl(baseUrl)
            .setParams(params)
            .setHeaders(headers)
            .setCookies(cookies)
            .setOkHttpClient(OkHttpClient())
            .setObjectMapper(ObjectMapper().registerKotlinModule())
            .onSuccess { responseBody ->
                println("06. HEAD ok - ${responseBody?.data}")
            }
            .onException { throwable: Throwable?, request: Request ->
                println("06. My ExceptionHandler - [${request.method}]${request.url} cause ${throwable?.message}")
            }
            .build()
            .execute()  // don't forget to execute
    }

    @Test
    fun testBuilderWithTypeReference() {
        // 测试使用TypeReference指定类型的Builder模式
        EasyHttpHead
            .Builder(object : TypeReference<String>() {})
            .setUrl(userHeadUrl)
            .setObjectMapper(ObjectMapper().registerKotlinModule())
            .onResponseSuccess { response ->
                println("07. HEAD ok - ${response.headers}")
            }
            .build()
            .execute()
    }

    @Test
    fun testCheckResourceExists() {
        // 测试检查资源是否存在
        EasyHttpHead.doRequestRaw(url = userHeadUrl) { response ->
            if (response.isSuccessful) {
                println("08. Resource exists - Status: ${response.code}")
            } else {
                println("08. Resource not found - Status: ${response.code}")
            }
        }
    }

    @Test
    fun testCheckFileMetadata() {
        // 测试获取文件元数据
        EasyHttpHead.doRequestRaw(url = fileHeadUrl) { response ->
            println("09. File metadata check:")
            println("    Status: ${response.code}")
            println("    Content-Length: ${response.header("Content-Length")} bytes")
            println("    Content-Type: ${response.header("Content-Type")}")
            println("    Last-Modified: ${response.header("Last-Modified")}")
            println("    Accept-Ranges: ${response.header("Accept-Ranges")}")
        }
    }

    @Test
    fun testCacheValidation() {
        // 测试缓存验证
        val cacheHeaders = mapOf(
            "If-None-Match" to "\"abc123\"",
            "If-Modified-Since" to "Wed, 21 Oct 2015 07:28:00 GMT"
        )
        
        EasyHttpHead.doRequestRaw(
            url = userHeadUrl,
            headers = cacheHeaders
        ) { response ->
            when (response.code) {
                304 -> println("10. Cache is still valid - Not Modified")
                200 -> println("10. Cache is stale - Resource has been modified")
                else -> println("10. Unexpected response - Status: ${response.code}")
            }
        }
    }
}