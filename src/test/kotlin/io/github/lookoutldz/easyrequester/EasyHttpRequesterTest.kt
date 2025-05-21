package io.github.lookoutldz.easyrequester

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.lookoutldz.easyrequester.requester.EasyHttpGet
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.jupiter.api.Test

class EasyHttpRequesterTest {

    @Test
    fun contextLoads() {
        println("ok")
    }

    // will return {data=2000, statusCode=0, statusMessage=SUCCESS}
    private val baseUrl = "http://127.0.0.1:58080/fetch-random-sleep"
    private val params = mapOf("millis" to "233")
    private val fullUrl = "$baseUrl?millis=${params["millis"]}"
    private val headers = mapOf("" to "")
    private val cookies = mapOf("" to "")

    data class ResponseBody(
        var data: String,
        var statusCode: Int,
        var statusMessage: String,
    )

    @Test
    fun testDoRequest() {

        // 1. simplest way
        EasyHttpGet.doRequestDefault(url = fullUrl) {
            // it is default to string
            println("01. ok - $it")
        }

        // 2. simplest way by type
        EasyHttpGet.doRequest<ResponseBody>(url = fullUrl) { responseBody ->
            println("02. ok - ${responseBody?.data}")
        }

        // 3. you can specify params
        EasyHttpGet.doRequest<ResponseBody>(
            url = baseUrl,
            params = params,
            headers = headers,
            cookies = cookies,
            okHttpClient = OkHttpClient(),
            objectMapper = ObjectMapper().registerKotlinModule(),
        ) { responseBody ->
            println("03. ok - ${responseBody?.data}")
        }

        // 4. you can implement your own exception handler
        EasyHttpGet.doRequest<ResponseBody>(
            url = fullUrl,
            objectMapper = ObjectMapper(),  // Exception here: this is not the correct ObjectMapper for Kotlin Data Class
            exceptionHandler = { throwable: Throwable?, request: Request ->
                println("04. My ExceptionHandler - [${request.method}]${request.url} cause ${throwable?.message}")
            }
        ) { responseBody ->
            println("04. ok - ${responseBody?.data}")
        }

        // 5. you can handle the whole raw response
        EasyHttpGet.doRequestRaw(url = fullUrl) { response ->
            println("05. ok - ${response.isSuccessful} - ${response.code} - ${response.message}")
        }

        // 6. you also can use builder to build a request
        EasyHttpGet
            .Builder(ResponseBody::class.java)
            .setUrl(baseUrl)
            .setParams(params)
            .setCookies(cookies)
            .setOkHttpClient(OkHttpClient())
            .setObjectMapper(ObjectMapper().registerKotlinModule())
            .onSuccess { responseBody ->
                println("06. ok - ${responseBody?.data}")
            }
            .onException { throwable: Throwable?, request: Request ->
                println("06. My ExceptionHandler - [${request.method}]${request.url} cause ${throwable?.message}")
            }
            .build()
            .execute()  // don't forget to execute

        // 7. you can use typeReference to specify a type
        EasyHttpGet
            .Builder(object : TypeReference<ResponseBody>() {})
            .setUrl(fullUrl)
            .onSuccess { responseBody ->
                println("07. ok - ${responseBody?.data}")
            }
            .build()
            .execute()
    }

}