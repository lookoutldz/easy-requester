package io.github.lookoutldz.easyrequester

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.lookoutldz.easyrequester.requester.EasyHttpGet
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.Test

class EasyHttpGetRequesterTest {

    @Test
    fun contextLoads() {
        println("get context ok")
    }

    // will return {data=2000, statusCode=0, statusMessage=SUCCESS}
    private val baseUrl = "http://127.0.0.1:58080/api/get/sleep/random"
    private val userUrl = "http://127.0.0.1:58080/api/get/user/random"
    private val params = mapOf("millis" to "233")
    private val fullUrl = "$baseUrl?millis=${params["millis"]}"
    private val headers = mapOf("" to "")
    private val cookies = mapOf("" to "")

    @Test
    fun testDoRequestDefault() {
        // 测试最简单的默认请求方式
        EasyHttpGet.doRequestDefault(url = userUrl) {
            // it is default to string
            println("ok - $it")
        }
    }

    @Test
    fun testDoRequestDefault2() {
        EasyHttpGet.doRequestDefault(url = userUrl, headers = headers) {
            println("ok - $it")
        }
    }

    @Test
    fun testDoRequestWithType() {
        // 测试指定返回类型的请求
        EasyHttpGet.doRequest<ResponseBody<*>>(url = userUrl) { responseBody ->
            println("ok - ${responseBody?.data}")
        }
    }

    @Test
    fun testDoRequestWithAllParams() {
        // 测试带有完整参数的请求
        EasyHttpGet.doRequest<ResponseBody<*>>(
            url = baseUrl,
            params = params,
            headers = headers,
            cookies = cookies,
            okHttpClient = OkHttpClient(),
            objectMapper = ObjectMapper().registerKotlinModule(),
        ) { responseBody ->
            println("ok - ${responseBody?.data}")
        }
    }

    @Test
    fun testDoRequestWithExceptionHandler() {
        // 测试自定义异常处理器
        EasyHttpGet.doRequest<ResponseBody<*>>(
            url = fullUrl,
            objectMapper = ObjectMapper(),  // Exception here: this is not the correct ObjectMapper for Kotlin Data Class
            exceptionHandler = { throwable: Throwable?, request: Request ->
                println("My ExceptionHandler - [${request.method}]${request.url} cause ${throwable?.message}")
            }
        ) { responseBody ->
            println("ok - ${responseBody?.data}")
        }
    }

    @Test
    fun testDoRequestRaw() {
        // 测试处理原始响应
        EasyHttpGet.doRequestRaw(url = fullUrl) { response ->
            println("ok - ${response.isSuccessful} - ${response.code} - ${response.message}")
        }
    }

    @Test
    fun testBuilderPattern() {
        // 测试使用Builder模式构建请求
        EasyHttpGet
            .Builder(ResponseBody::class.java)
            .setUrl(baseUrl)
            .setParams(params)
            .setCookies(cookies)
            .setOkHttpClient(OkHttpClient())
            .setObjectMapper(ObjectMapper().registerKotlinModule())
            .onSuccess { responseBody ->
                println("ok - ${responseBody?.data}")
            }
            .onException { throwable: Throwable?, request: Request ->
                println("My ExceptionHandler - [${request.method}]${request.url} cause ${throwable?.message}")
            }
            .build()
            .execute()  // don't forget to execute
    }

    @Test
    fun testBuilderWithTypeReference() {
        // 测试使用TypeReference指定类型的Builder模式
        EasyHttpGet
            .Builder(object : TypeReference<ResponseBody<User>>() {})
            .setUrl(userUrl)
            .onSuccess { responseBody ->
                println("ok - ${responseBody?.data?.name}")
            }
            .build()
            .execute()
    }

}