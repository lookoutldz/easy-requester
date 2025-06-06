package io.github.lookoutldz.easyrequester

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.lookoutldz.easyrequester.requester.EasyHttpPost
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.jupiter.api.Test

/**
 *  @author looko
 *  @date 2025/5/26
 *
 */
class EasyHttpPostRequesterTest {

    private val baseUrl = "http://localhost:58080/api/post"
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun contextLoads() {
        println("post context ok")
    }

    /**
     * simple post
     */
    @Test
    fun post() {
        val simplePostUrl = "http://localhost:58080/api/post/"
        EasyHttpPost.doRequestDefault(simplePostUrl) {
            println(it)
        }
    }

    @Test
    fun postUser() {
        val simplePostUrl = "http://localhost:58080/api/post/user"
        val jsonUser = ObjectMapper().registerKotlinModule().writeValueAsString(User(114514, "Dark"))
        println(jsonUser)
        EasyHttpPost.doRequestDefault(url = simplePostUrl, body = jsonUser) {
            println(it)
        }
    }

    /**
     * 测试JSON Content-Type接口
     */
    @Test
    fun testJsonPost() {
        val url = "$baseUrl/json"
        val user = User(123, "JsonUser")
        val jsonBody = objectMapper.writeValueAsString(user)
        
        EasyHttpPost.doRequestDefault(
            url = url,
            body = jsonBody,
            contentType = "application/json"
        ) { response ->
            println("JSON POST Response: $response")
        }
    }

    /**
     * 测试XML Content-Type接口
     */
    @Test
    fun testXmlPost() {
        val url = "$baseUrl/xml"
        val xmlBody = """<?xml version="1.0" encoding="UTF-8"?>
            <user>
                <userId>456</userId>
                <name>XmlUser</name>
            </user>""".trimIndent()

        // both ok
        val requestBody = xmlBody.toRequestBody("text/xml".toMediaType())
        
        EasyHttpPost.doRequestDefault(
            url = url,
//            body = xmlBody,
            body = requestBody,
//            contentType = "application/xml"
        ) { response ->
            println("XML POST Response: $response")
        }
    }

    /**
     * 测试Form URL Encoded Content-Type接口
     */
    @Test
    fun testFormPost() {
        val url = "$baseUrl/form"
        val formBody = "id=789&name=FormUser"
        
        EasyHttpPost.doRequestDefault(
            url = url,
            body = formBody,
            contentType = "application/x-www-form-urlencoded"
        ) { response ->
            println("Form POST Response: $response")
        }
    }

    /**
     * 测试Multipart Form Data Content-Type接口
     */
    @Test
    fun testMultipartPost() {
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
        
        EasyHttpPost.doRequestDefault(
            url = url,
            body = multipartBody
            // 移除 contentType 参数，让 MultipartBody 使用自己的 Content-Type
        ) { response ->
            println("Multipart POST Response: $response")
        }
    }

    /**
     * 测试Text Content-Type接口
     */
    @Test
    fun testTextPost() {
        val url = "$baseUrl/text"
        val textContent = "This is a plain text message for testing"
        
        EasyHttpPost.doRequestDefault(
            url = url,
            body = textContent,
            contentType = "text/plain"
        ) { response ->
            println("Text POST Response: $response")
        }
    }

    /**
     * 测试接受任意Content-Type的接口
     */
    @Test
    fun testAnyPost() {
        val url = "$baseUrl/any"
        val anyContent = "Any content type test"
        
        EasyHttpPost.doRequestDefault(
            url = url,
            body = anyContent
        ) { response ->
            println("Any POST Response: $response")
        }
    }

    /**
     * 测试使用Builder模式的JSON请求
     */
    @Test
    fun testJsonPostWithBuilder() {
        val url = "$baseUrl/json"
        val user = User(555, "BuilderUser")
        val jsonBody = objectMapper.writeValueAsString(user)
        
        EasyHttpPost.Builder(String::class.java)
            .setUrl(url)
            .setBody(jsonBody)
            .setContentType("application/json")
            .onSuccess { response ->
                println("Builder JSON POST Response: $response")
            }
            .onException { throwable, request ->
                println("Builder JSON POST Error: ${throwable.message}")
            }
            .build()
            .execute()
    }

    /**
     * 测试带自定义Headers的请求
     */
    @Test
    fun testPostWithHeaders() {
        val url = "$baseUrl/json"
        val user = User(777, "HeaderUser")
        val jsonBody = objectMapper.writeValueAsString(user)
        val headers = mapOf(
            "Authorization" to "Bearer test-token",
            "X-Custom-Header" to "custom-value"
        )
        
        EasyHttpPost.doRequestDefault(
            url = url,
            body = jsonBody,
            contentType = "application/json",
            headers = headers
        ) { response ->
            println("POST with Headers Response: $response")
        }
    }
}