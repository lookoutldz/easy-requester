package io.github.lookoutldz.easyrequester

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.lookoutldz.easyrequester.requester.EasyHttpPut
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.jupiter.api.Test

/**
 *  @author looko
 *  @date 2025/6/6
 *  PUT请求测试类
 */
class EasyHttpPutRequesterTest {

    private val baseUrl = "http://localhost:58080/api/put"
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun contextLoads() {
        println("put context ok")
    }

    /**
     * simple put
     */
    @Test
    fun put() {
        val simplePutUrl = "http://localhost:58080/api/put/"
        EasyHttpPut.doRequestDefault(simplePutUrl) {
            println(it)
        }
    }

    @Test
    fun putUser() {
        val simplePutUrl = "http://localhost:58080/api/put/user"
        val jsonUser = ObjectMapper().registerKotlinModule().writeValueAsString(User(114514, "Dark"))
        println(jsonUser)
        EasyHttpPut.doRequestDefault(url = simplePutUrl, body = jsonUser) {
            println(it)
        }
    }

    /**
     * 测试JSON Content-Type接口
     */
    @Test
    fun testJsonPut() {
        val url = "$baseUrl/json"
        val user = User(123, "JsonUser")
        val jsonBody = objectMapper.writeValueAsString(user)
        
        EasyHttpPut.doRequestDefault(
            url = url,
            body = jsonBody,
            contentType = "application/json"
        ) { response ->
            println("JSON PUT Response: $response")
        }
    }

    /**
     * 测试XML Content-Type接口
     */
    @Test
    fun testXmlPut() {
        val url = "$baseUrl/xml"
        val xmlBody = """<?xml version="1.0" encoding="UTF-8"?>
            <user>
                <userId>456</userId>
                <name>XmlUser</name>
            </user>""".trimIndent()

        // both ok
        val requestBody = xmlBody.toRequestBody("text/xml".toMediaType())
        
        EasyHttpPut.doRequestDefault(
            url = url,
//            body = xmlBody,
            body = requestBody,
//            contentType = "application/xml"
        ) { response ->
            println("XML PUT Response: $response")
        }
    }

    /**
     * 测试Form URL Encoded Content-Type接口
     */
    @Test
    fun testFormPut() {
        val url = "$baseUrl/form"
        val formBody = "id=789&name=FormUser"
        
        EasyHttpPut.doRequestDefault(
            url = url,
            body = formBody,
            contentType = "application/x-www-form-urlencoded"
        ) { response ->
            println("Form PUT Response: $response")
        }
    }

    /**
     * 测试Multipart Form Data Content-Type接口
     */
    @Test
    fun testMultipartPut() {
        val url = "$baseUrl/multipart"
        
        // 创建multipart body
        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("id", "999")
            .addFormDataPart("name", "MultipartUser")
            .addFormDataPart(
                "file", 
                "test.txt",
                "This is test file content".toRequestBody("text/plain".toMediaType())
            )
            .build()
        
        EasyHttpPut.doRequestDefault(
            url = url,
            body = multipartBody
            // 移除 contentType 参数，让 MultipartBody 使用自己的 Content-Type
        ) { response ->
            println("Multipart PUT Response: $response")
        }
    }

    /**
     * 测试Text Content-Type接口
     */
    @Test
    fun testTextPut() {
        val url = "$baseUrl/text"
        val textContent = "This is a plain text message for testing"
        
        EasyHttpPut.doRequestDefault(
            url = url,
            body = textContent,
            contentType = "text/plain"
        ) { response ->
            println("Text PUT Response: $response")
        }
    }

    /**
     * 测试接受任意Content-Type的接口
     */
    @Test
    fun testAnyPut() {
        val url = "$baseUrl/any"
        val anyContent = "Any content type test"
        
        EasyHttpPut.doRequestDefault(
            url = url,
            body = anyContent
        ) { response ->
            println("Any PUT Response: $response")
        }
    }

    /**
     * 测试使用Builder模式的JSON请求
     */
    @Test
    fun testJsonPutWithBuilder() {
        val url = "$baseUrl/json"
        val user = User(555, "BuilderUser")
        val jsonBody = objectMapper.writeValueAsString(user)
        
        EasyHttpPut.Builder(String::class.java)
            .setUrl(url)
            .setBody(jsonBody)
            .setContentType("application/json")
            .onSuccess { response ->
                println("Builder JSON PUT Response: $response")
            }
            .onException { throwable, request ->
                println("Builder JSON PUT Error: ${throwable.message}")
            }
            .build()
            .execute()
    }

    /**
     * 测试带自定义Headers的请求
     */
    @Test
    fun testPutWithHeaders() {
        val url = "$baseUrl/json"
        val user = User(777, "HeaderUser")
        val jsonBody = objectMapper.writeValueAsString(user)
        val headers = mapOf(
            "Authorization" to "Bearer test-token",
            "X-Custom-Header" to "custom-value"
        )
        
        EasyHttpPut.doRequestDefault(
            url = url,
            body = jsonBody,
            contentType = "application/json",
            headers = headers
        ) { response ->
            println("PUT with Headers Response: $response")
        }
    }

    /**
     * 测试PUT请求更新用户信息
     */
    @Test
    fun testPutUpdateUser() {
        val url = "$baseUrl/user/123"
        val updatedUser = User(123, "UpdatedUserName")
        
        EasyHttpPut.doRequest<User>(
            url = url,
            body = updatedUser
        ) { user ->
            println("Updated User: $user")
        }
    }

    /**
     * 测试PUT请求替换资源
     */
    @Test
    fun testPutReplaceResource() {
        val url = "$baseUrl/resource/456"
        val resourceData = mapOf(
            "id" to 456,
            "title" to "New Resource Title",
            "content" to "Complete new content for the resource",
            "status" to "active"
        )
        
        EasyHttpPut.doRequestDefault(
            url = url,
            body = objectMapper.writeValueAsString(resourceData),
            contentType = "application/json"
        ) { response ->
            println("Replace Resource Response: $response")
        }
    }
}