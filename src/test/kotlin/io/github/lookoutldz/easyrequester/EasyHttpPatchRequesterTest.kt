package io.github.lookoutldz.easyrequester

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.lookoutldz.easyrequester.requester.EasyHttpPatch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.jupiter.api.Test

/**
 *  @author looko
 *  @date 2025/6/6
 *  PATCH请求测试类
 */
class EasyHttpPatchRequesterTest {

    private val baseUrl = "http://localhost:58080/api/patch"
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun contextLoads() {
        println("patch context ok")
    }

    /**
     * simple patch
     */
    @Test
    fun patch() {
        val simplePatchUrl = "http://localhost:58080/api/patch/"
        EasyHttpPatch.doRequestDefault(simplePatchUrl) {
            println(it)
        }
    }

    @Test
    fun patchUser() {
        val simplePatchUrl = "http://localhost:58080/api/patch/user"
        val patchData = mapOf("name" to "PatchedName")
        val jsonPatch = ObjectMapper().registerKotlinModule().writeValueAsString(patchData)
        println(jsonPatch)
        EasyHttpPatch.doRequestDefault(url = simplePatchUrl, body = jsonPatch) {
            println(it)
        }
    }

    /**
     * 测试JSON Content-Type接口
     */
    @Test
    fun testJsonPatch() {
        val url = "$baseUrl/json"
        val patchData = mapOf("name" to "JsonPatchedUser")
        val jsonBody = objectMapper.writeValueAsString(patchData)
        
        EasyHttpPatch.doRequestDefault(
            url = url,
            body = jsonBody,
            contentType = "application/json"
        ) { response ->
            println("JSON PATCH Response: $response")
        }
    }

    /**
     * 测试XML Content-Type接口
     */
    @Test
    fun testXmlPatch() {
        val url = "$baseUrl/xml"
        val xmlBody = """<?xml version="1.0" encoding="UTF-8"?>
            <patch>
                <name>XmlPatchedUser</name>
            </patch>""".trimIndent()

        // both ok
        val requestBody = xmlBody.toRequestBody("text/xml".toMediaType())
        
        EasyHttpPatch.doRequestDefault(
            url = url,
//            body = xmlBody,
            body = requestBody,
//            contentType = "application/xml"
        ) { response ->
            println("XML PATCH Response: $response")
        }
    }

    /**
     * 测试Form URL Encoded Content-Type接口
     */
    @Test
    fun testFormPatch() {
        val url = "$baseUrl/form"
        val formBody = "name=FormPatchedUser"
        
        EasyHttpPatch.doRequestDefault(
            url = url,
            body = formBody,
            contentType = "application/x-www-form-urlencoded"
        ) { response ->
            println("Form PATCH Response: $response")
        }
    }

    /**
     * 测试Multipart Form Data Content-Type接口
     */
    @Test
    fun testMultipartPatch() {
        val url = "$baseUrl/multipart"
        
        // 创建multipart body
        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("name", "MultipartPatchedUser")
            .addFormDataPart(
                "file", 
                "patch.txt",
                "This is patch file content".toRequestBody("text/plain".toMediaType())
            )
            .build()
        
        EasyHttpPatch.doRequestDefault(
            url = url,
            body = multipartBody
            // 移除 contentType 参数，让 MultipartBody 使用自己的 Content-Type
        ) { response ->
            println("Multipart PATCH Response: $response")
        }
    }

    /**
     * 测试Text Content-Type接口
     */
    @Test
    fun testTextPatch() {
        val url = "$baseUrl/text"
        val textContent = "This is a plain text patch message"
        
        EasyHttpPatch.doRequestDefault(
            url = url,
            body = textContent,
            contentType = "text/plain"
        ) { response ->
            println("Text PATCH Response: $response")
        }
    }

    /**
     * 测试接受任意Content-Type的接口
     */
    @Test
    fun testAnyPatch() {
        val url = "$baseUrl/any"
        val anyContent = "Any content type patch test"
        
        EasyHttpPatch.doRequestDefault(
            url = url,
            body = anyContent
        ) { response ->
            println("Any PATCH Response: $response")
        }
    }

    /**
     * 测试使用Builder模式的JSON请求
     */
    @Test
    fun testJsonPatchWithBuilder() {
        val url = "$baseUrl/json"
        val patchData = mapOf("name" to "BuilderPatchedUser")
        val jsonBody = objectMapper.writeValueAsString(patchData)
        
        EasyHttpPatch.Builder(String::class.java)
            .setUrl(url)
            .setBody(jsonBody)
            .setContentType("application/json")
            .onSuccess { response ->
                println("Builder JSON PATCH Response: $response")
            }
            .onException { throwable, request ->
                println("Builder JSON PATCH Error: ${throwable.message}")
            }
            .build()
            .execute()
    }

    /**
     * 测试带自定义Headers的请求
     */
    @Test
    fun testPatchWithHeaders() {
        val url = "$baseUrl/json"
        val patchData = mapOf("name" to "HeaderPatchedUser")
        val jsonBody = objectMapper.writeValueAsString(patchData)
        val headers = mapOf(
            "Authorization" to "Bearer test-token",
            "X-Custom-Header" to "custom-value"
        )
        
        EasyHttpPatch.doRequestDefault(
            url = url,
            body = jsonBody,
            contentType = "application/json",
            headers = headers
        ) { response ->
            println("PATCH with Headers Response: $response")
        }
    }

    /**
     * 测试PATCH请求部分更新用户信息
     */
    @Test
    fun testPatchPartialUpdateUser() {
        val url = "$baseUrl/user/123"
        val partialUpdate = mapOf("name" to "PartiallyUpdatedName")
        // 复杂类型用Builder
        EasyHttpPatch
            .Builder(object : TypeReference<ResponseBody<User>>() {})
            .setUrl(url)
            .setBody(partialUpdate)
            .onSuccess { response ->
                println("Patch JSON PATCH Response: ${response?.data}")
            }
            .build()
            .execute()
        // 复杂类型不支持直接doRequest，以下为错误示范
//        EasyHttpPatch.doRequest<ResponseBody<User>>(
//            url = url,
//            body = partialUpdate,
//        ) { resp ->
//            println("Partially Updated User: ${resp?.data}")
//        }
    }

    /**
     * 测试JSON Patch格式
     */
    @Test
    fun testJsonPatchFormat() {
        val url = "$baseUrl/jsonpatch"
        val jsonPatchOps = listOf(
            mapOf(
                "op" to "replace",
                "path" to "/name",
                "value" to "JsonPatchedName"
            ),
            mapOf(
                "op" to "add",
                "path" to "/email",
                "value" to "jsonpatch@example.com"
            )
        )
        
        EasyHttpPatch.doRequestDefault(
            url = url,
            body = objectMapper.writeValueAsString(jsonPatchOps),
            contentType = "application/json-patch+json"
        ) { response ->
            println("JSON Patch Format Response: $response")
        }
    }

    /**
     * 测试PATCH请求更新资源状态
     */
    @Test
    fun testPatchResourceStatus() {
        val url = "$baseUrl/resource/456/status"
        val statusUpdate = mapOf("status" to "inactive")
        
        EasyHttpPatch.doRequestDefault(
            url = url,
            body = objectMapper.writeValueAsString(statusUpdate),
            contentType = "application/json"
        ) { response ->
            println("Update Resource Status Response: $response")
        }
    }
}