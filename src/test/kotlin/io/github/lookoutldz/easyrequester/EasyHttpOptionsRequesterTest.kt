package io.github.lookoutldz.easyrequester

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.lookoutldz.easyrequester.requester.EasyHttpOptions
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.Test

class EasyHttpOptionsRequesterTest {

    @Test
    fun contextLoads() {
        println("options context ok")
    }

    // 测试用的URL - OPTIONS通常用于CORS预检和获取允许的方法
    private val baseUrl = "http://127.0.0.1:58080/api/options/test"
    private val corsUrl = "http://127.0.0.1:58080/api/cors/check"
    private val params = mapOf("check" to "methods")
    private val headers = mapOf(
        "Origin" to "http://localhost:3000",
        "Access-Control-Request-Method" to "POST",
        "Access-Control-Request-Headers" to "Content-Type"
    )
    private val cookies = mapOf("session" to "test123")

    @Test
    fun testDoRequestDefault() {
        // 测试最简单的默认OPTIONS请求方式
        EasyHttpOptions.doRequest<String>(url = baseUrl) {
            println("01. OPTIONS default - $it")
        }
    }

    @Test
    fun testDoRequestWithType() {
        // 测试指定返回类型的OPTIONS请求
        EasyHttpOptions.doRequest<ResponseBody<*>>(url = baseUrl) { responseBody ->
            println("02. OPTIONS with type - ${responseBody?.data}")
        }
    }

    @Test
    fun testDoRequestWithAllParams() {
        // 测试带有完整参数的OPTIONS请求
        EasyHttpOptions.doRequest<ResponseBody<*>>(
            url = baseUrl,
            params = params,
            headers = headers,
            cookies = cookies,
            okHttpClient = OkHttpClient(),
            objectMapper = ObjectMapper().registerKotlinModule(),
        ) { responseBody ->
            println("03. OPTIONS with all params - ${responseBody?.data}")
        }
    }

    @Test
    fun testDoRequestWithExceptionHandler() {
        // 测试自定义异常处理器
        EasyHttpOptions.doRequest<ResponseBody<*>>(
            url = baseUrl,
            objectMapper = ObjectMapper(),  // 可能会有异常
            exceptionHandler = { throwable: Throwable?, request: Request ->
                println("04. OPTIONS ExceptionHandler - [${request.method}]${request.url} cause ${throwable?.message}")
            }
        ) { responseBody ->
            println("04. OPTIONS ok - ${responseBody?.data}")
        }
    }

    @Test
    fun testDoRequestRaw() {
        // 测试处理原始响应 - OPTIONS请求通常需要检查响应头
        EasyHttpOptions.doRequest<String>(
            url = corsUrl,
            responseHandler = { response ->
                println("05. OPTIONS raw response:")
                println("   Status: ${response.code} - ${response.message}")
                println("   Allow: ${response.header("Allow")}")
                println("   CORS Methods: ${response.header("Access-Control-Allow-Methods")}")
                println("   CORS Headers: ${response.header("Access-Control-Allow-Headers")}")
                println("   CORS Origin: ${response.header("Access-Control-Allow-Origin")}")
            }
        ) { result ->
            println("05. OPTIONS result - $result")
        }
    }

    @Test
    fun testBuilderPattern() {
        // 测试使用Builder模式构建OPTIONS请求
        EasyHttpOptions
            .Builder(ResponseBody::class.java)
            .setUrl(baseUrl)
            .setParams(params)
            .setHeaders(headers)
            .setCookies(cookies)
            .setOkHttpClient(OkHttpClient())
            .setObjectMapper(ObjectMapper().registerKotlinModule())
            .onSuccess { responseBody ->
                println("06. OPTIONS builder ok - ${responseBody?.data}")
            }
            .onException { throwable: Throwable?, request: Request ->
                println("06. OPTIONS builder ExceptionHandler - [${request.method}]${request.url} cause ${throwable?.message}")
            }
            .build()
            .execute()  // 不要忘记执行
    }

    @Test
    fun testCorsPreflightRequest() {
        // 测试CORS预检请求场景
        EasyHttpOptions.doRequest<String>(
            url = corsUrl,
            headers = mapOf(
                "Origin" to "https://example.com",
                "Access-Control-Request-Method" to "PUT",
                "Access-Control-Request-Headers" to "Content-Type,Authorization"
            ),
            responseHandler = { response ->
                println("07. CORS preflight response:")
                println("   Status: ${response.code}")
                println("   Allowed Methods: ${response.header("Access-Control-Allow-Methods")}")
                println("   Allowed Headers: ${response.header("Access-Control-Allow-Headers")}")
                println("   Max Age: ${response.header("Access-Control-Max-Age")}")
            }
        ) { result ->
            println("07. CORS preflight result - $result")
        }
    }


}