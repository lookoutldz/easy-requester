package io.github.lookoutldz.easyrequester

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.lookoutldz.easyrequester.requester.EasyHttpTrace
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.Test

class EasyHttpTraceRequesterTest {

    @Test
    fun contextLoads() {
        println("trace context ok")
    }

    // 测试用的URL - TRACE通常用于调试和诊断
    private val baseUrl = "http://127.0.0.1:58080/api/trace/test"
    private val debugUrl = "http://127.0.0.1:58080/api/debug/trace"
    private val params = mapOf("debug" to "true", "level" to "verbose")
    private val headers = mapOf(
        "X-Debug" to "true",
        "X-Request-ID" to "trace-test-12345",
        "X-Trace-Level" to "detailed"
    )
    private val cookies = mapOf("debug-session" to "trace123")

    @Test
    fun testDoRequestDefault() {
        // 测试最简单的默认TRACE请求方式
        EasyHttpTrace.doRequest<String>(url = baseUrl) {
            println("01. TRACE default - $it")
        }
    }

    @Test
    fun testDoRequestWithType() {
        // 测试指定返回类型的TRACE请求
        EasyHttpTrace.doRequest<ResponseBody<*>>(url = baseUrl) { responseBody ->
            println("02. TRACE with type - ${responseBody?.data}")
        }
    }

    @Test
    fun testDoRequestWithAllParams() {
        // 测试带有完整参数的TRACE请求
        EasyHttpTrace.doRequest<ResponseBody<*>>(
            url = baseUrl,
            params = params,
            headers = headers,
            cookies = cookies,
            okHttpClient = OkHttpClient(),
            objectMapper = ObjectMapper().registerKotlinModule(),
        ) { responseBody ->
            println("03. TRACE with all params - ${responseBody?.data}")
        }
    }

    @Test
    fun testDoRequestWithExceptionHandler() {
        // 测试自定义异常处理器
        EasyHttpTrace.doRequest<ResponseBody<*>>(
            url = baseUrl,
            objectMapper = ObjectMapper(),  // 可能会有异常
            exceptionHandler = { throwable: Throwable?, request: Request ->
                println("04. TRACE ExceptionHandler - [${request.method}]${request.url} cause ${throwable?.message}")
            }
        ) { responseBody ->
            println("04. TRACE ok - ${responseBody?.data}")
        }
    }

    @Test
    fun testDoRequestRaw() {
        // 测试处理原始响应 - TRACE请求通常返回请求路径信息
        EasyHttpTrace.doRequest<String>(
            url = debugUrl,
            responseHandler = { response ->
                println("05. TRACE raw response:")
                println("   Status: ${response.code} - ${response.message}")
                println("   Content-Type: ${response.header("Content-Type")}")
                println("   Server: ${response.header("Server")}")
                println("   Via: ${response.header("Via")}")
                val body = response.body?.string()
                println("   Body: $body")
            }
        ) { result ->
            println("05. TRACE result - $result")
        }
    }

    @Test
    fun testBuilderPattern() {
        // 测试使用Builder模式构建TRACE请求
        EasyHttpTrace
            .Builder(ResponseBody::class.java)
            .setUrl(baseUrl)
            .setParams(params)
            .setHeaders(headers)
            .setCookies(cookies)
            .setOkHttpClient(OkHttpClient())
            .setObjectMapper(ObjectMapper().registerKotlinModule())
            .onSuccess { responseBody ->
                println("06. TRACE builder ok - ${responseBody?.data}")
            }
            .onException { throwable: Throwable?, request: Request ->
                println("06. TRACE builder ExceptionHandler - [${request.method}]${request.url} cause ${throwable?.message}")
            }
            .build()
            .execute()  // 不要忘记执行
    }

    @Test
    fun testDebugTraceRequest() {
        // 测试调试场景的TRACE请求
        EasyHttpTrace.doRequest<String>(
            url = debugUrl,
            headers = mapOf(
                "X-Debug" to "true",
                "X-Request-ID" to "debug-trace-001",
                "X-Client-Info" to "EasyRequester-Test"
            ),
            responseHandler = { response ->
                println("07. Debug TRACE response:")
                println("   Status: ${response.code}")
                println("   Request Path Info: ${response.body?.string()}")
                println("   Response Headers:")
                response.headers.forEach { (name, value) ->
                    println("     $name: $value")
                }
            }
        ) { result ->
            println("07. Debug TRACE result - $result")
        }
    }

    @Test
    fun testProxyTraceRequest() {
        // 测试通过代理的TRACE请求（用于诊断代理行为）
        EasyHttpTrace.doRequest<String>(
            url = baseUrl,
            headers = mapOf(
                "X-Forwarded-For" to "192.168.1.100",
                "X-Real-IP" to "203.0.113.1",
                "Via" to "1.1 proxy.example.com"
            ),
            responseHandler = { response ->
                println("08. Proxy TRACE response:")
                println("   Via Header: ${response.header("Via")}")
                println("   X-Forwarded-* Headers:")
                response.headers.filter { it.first.startsWith("X-Forwarded") }
                    .forEach { (name, value) ->
                        println("     $name: $value")
                    }
            }
        ) { result ->
            println("08. Proxy TRACE result - $result")
        }
    }

}