package io.github.lookoutldz.easyrequester

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.lookoutldz.easyrequester.requester.EasyHttpDelete
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.Test

/**
 *  @author looko
 *  @date 2025/6/6
 *  DELETE请求测试类
 */
class EasyHttpDeleteRequesterTest {

    @Test
    fun contextLoads() {
        println("delete context ok")
    }

    // DELETE请求通常用于删除资源
    private val baseUrl = "http://127.0.0.1:58080/api/delete"
    private val userDeleteUrl = "http://127.0.0.1:58080/api/delete/user/123"
    private val params = mapOf("force" to "true")
    private val headers = mapOf("Authorization" to "Bearer token123")
    private val cookies = mapOf("sessionId" to "abc123")

    @Test
    fun testDoRequestDefault() {
        // 测试最简单的默认DELETE请求方式
        EasyHttpDelete.doRequestDefault(url = userDeleteUrl) {
            // it is default to string
            println("01. DELETE ok - $it")
        }
    }

    @Test
    fun testDoRequestWithType() {
        // 测试指定返回类型的DELETE请求
        EasyHttpDelete.doRequest<ResponseBody<*>>(url = userDeleteUrl) { responseBody ->
            println("02. DELETE ok - ${responseBody?.data}")
        }
    }

    @Test
    fun testDoRequestWithAllParams() {
        // 测试带有完整参数的DELETE请求
        EasyHttpDelete.doRequest<ResponseBody<*>>(
            url = baseUrl,
            params = params,
            headers = headers,
            cookies = cookies,
            okHttpClient = OkHttpClient(),
            objectMapper = ObjectMapper().registerKotlinModule(),
        ) { responseBody ->
            println("03. DELETE ok - ${responseBody?.data}")
        }
    }

    @Test
    fun testDoRequestWithExceptionHandler() {
        // 测试自定义异常处理器
        EasyHttpDelete.doRequest<ResponseBody<*>>(
            url = userDeleteUrl,
            objectMapper = ObjectMapper(),  // Exception here: this is not the correct ObjectMapper for Kotlin Data Class
            exceptionHandler = { throwable: Throwable?, request: Request ->
                println("04. My ExceptionHandler - [${request.method}]${request.url} cause ${throwable?.message}")
            }
        ) { responseBody ->
            println("04. DELETE ok - ${responseBody?.data}")
        }
    }

    @Test
    fun testDoRequestRaw() {
        // 测试处理原始响应
        EasyHttpDelete.doRequestRaw(url = userDeleteUrl) { response ->
            println("05. DELETE ok - ${response.isSuccessful} - ${response.code} - ${response.message}")
        }
    }

    @Test
    fun testBuilderPattern() {
        // 测试使用Builder模式构建DELETE请求
        EasyHttpDelete
            .Builder(ResponseBody::class.java)
            .setUrl(baseUrl)
            .setParams(params)
            .setHeaders(headers)
            .setCookies(cookies)
            .setOkHttpClient(OkHttpClient())
            .setObjectMapper(ObjectMapper().registerKotlinModule())
            .onSuccess { responseBody ->
                println("06. DELETE ok - ${responseBody?.data}")
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
        EasyHttpDelete
            .Builder(object : TypeReference<ResponseBody<User>>() {})
            .setUrl(userDeleteUrl)
            .onSuccess { responseBody ->
                println("07. DELETE ok - ${responseBody?.data?.name}")
            }
            .build()
            .execute()
    }

    @Test
    fun testDeleteWithConfirmation() {
        // 测试需要确认的删除操作
        val confirmDeleteUrl = "$baseUrl/confirm"
        val confirmParams = mapOf("confirm" to "yes", "reason" to "test")
        
        EasyHttpDelete.doRequest<ResponseBody<String>>(
            url = confirmDeleteUrl,
            params = confirmParams,
            headers = mapOf("X-Confirm-Delete" to "true")
        ) { responseBody ->
            println("08. Confirmed DELETE ok - ${responseBody?.statusMessage}")
        }
    }

    @Test
    fun testBatchDelete() {
        // 测试批量删除
        val batchDeleteUrl = "$baseUrl/batch"
        val batchParams = mapOf("ids" to "1,2,3,4,5")
        
        EasyHttpDelete.doRequestDefault(
            url = batchDeleteUrl,
            params = batchParams
        ) { response ->
            println("09. Batch DELETE ok - $response")
        }
    }
}