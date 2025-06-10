package io.github.lookoutldz.easyrequester;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import io.github.lookoutldz.easyrequester.requester4j.EasyHttpPut4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class JavaHttpPutTest {

    private static final String baseUrl = "http://localhost:58080/api/put";
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new KotlinModule.Builder().build());

    @Test
    public void contextLoads() {
        System.out.println("put context ok");
    }

    /**
     * simple put
     */
    @Test
    public void put() {
        String simplePutUrl = "http://localhost:58080/api/put/";
        EasyHttpPut4j.doRequestDefault(simplePutUrl, null, null, System.out::println);
    }

    @Test
    public void putUser() {
        String simplePutUrl = "http://localhost:58080/api/put/user";
        try {
            String jsonUser = objectMapper.writeValueAsString(new User(114514, "Dark"));
            System.out.println(jsonUser);
            EasyHttpPut4j.doRequestDefault(simplePutUrl, jsonUser, null, System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试JSON Content-Type接口
     */
    @Test
    public void testJsonPut() {
        String url = baseUrl + "/json";
        User user = new User(123, "JsonUser");
        try {
            String jsonBody = objectMapper.writeValueAsString(user);
            EasyHttpPut4j.doRequestDefault(
                url,
                jsonBody,
                "application/json",
                response -> System.out.println("JSON PUT Response: " + response)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试XML Content-Type接口
     */
    @Test
    public void testXmlPut() {
        String url = baseUrl + "/xml";
        String xmlBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<user>\n" +
                "    <userId>456</userId>\n" +
                "    <name>XmlUser</name>\n" +
                "</user>";

        RequestBody requestBody = RequestBody.create(xmlBody, MediaType.get("text/xml"));
        
        EasyHttpPut4j.doRequestDefault(
            url,
            requestBody,
            null,
            response -> System.out.println("XML PUT Response: " + response)
        );
    }

    /**
     * 测试Form URL Encoded Content-Type接口
     */
    @Test
    public void testFormPut() {
        String url = baseUrl + "/form";
        String formBody = "id=789&name=FormUser";
        
        EasyHttpPut4j.doRequestDefault(
            url,
            formBody,
            "application/x-www-form-urlencoded",
            response -> System.out.println("Form PUT Response: " + response)
        );
    }

    /**
     * 测试Multipart Form Data Content-Type接口
     */
    @Test
    public void testMultipartPut() {
        String url = baseUrl + "/multipart";
        
        MultipartBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", "999")
                .addFormDataPart("name", "MultipartUser")
                .addFormDataPart(
                    "file", 
                    "test.txt",
                    RequestBody.create("This is test file content", MediaType.get("text/plain"))
                )
                .build();
        
        EasyHttpPut4j.doRequestDefault(
            url,
            multipartBody,
            null,
            response -> System.out.println("Multipart PUT Response: " + response)
        );
    }

    /**
     * 测试Text Content-Type接口
     */
    @Test
    public void testTextPut() {
        String url = baseUrl + "/text";
        String textContent = "This is a plain text message for testing";
        
        EasyHttpPut4j.doRequestDefault(
            url,
            textContent,
            "text/plain",
            response -> System.out.println("Text PUT Response: " + response)
        );
    }

    /**
     * 测试接受任意Content-Type的接口
     */
    @Test
    public void testAnyPut() {
        String url = baseUrl + "/any";
        String anyContent = "Any content type test";
        
        EasyHttpPut4j.doRequestDefault(
            url,
            anyContent,
            null,
            response -> System.out.println("Any PUT Response: " + response)
        );
    }

    /**
     * 测试使用Builder模式的JSON请求
     */
    @Test
    public void testJsonPutWithBuilder() {
        String url = baseUrl + "/json";
        User user = new User(555, "BuilderUser");
        try {
            String jsonBody = objectMapper.writeValueAsString(user);
            
            new EasyHttpPut4j.Builder<>(String.class)
                    .setUrl(url)
                    .setBody(jsonBody)
                    .setContentType("application/json")
                    .onSuccess(response -> System.out.println("Builder JSON PUT Response: " + response))
                    .onException((throwable, request) -> System.out.println("Builder JSON PUT Error: " + throwable.getMessage()))
                    .build()
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试带自定义Headers的请求
     */
    @Test
    public void testPutWithHeaders() {
        String url = baseUrl + "/json";
        User user = new User(777, "HeaderUser");
        try {
            String jsonBody = objectMapper.writeValueAsString(user);
            Map<String, String> headers = Map.of(
                "Authorization", "Bearer test-token",
                "X-Custom-Header", "custom-value"
            );
            
            new EasyHttpPut4j.Builder<>(String.class)
                    .setUrl(url)
                    .setBody(jsonBody)
                    .setContentType("application/json")
                    .setHeaders(headers)
                    .onSuccess(response -> System.out.println("PUT with Headers Response: " + response))
                    .build()
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试PUT请求更新用户信息
     */
    @Test
    public void testPutUpdateUser() {
        String url = baseUrl + "/user/123";
        User updatedUser = new User(123, "UpdatedUserName");
        
        EasyHttpPut4j.doRequest(
            User.class,
            url,
            updatedUser,
            null,
            user -> System.out.println("Updated User: " + user)
        );
    }

    /**
     * 测试PUT请求替换资源
     */
    @Test
    public void testPutReplaceResource() {
        String url = baseUrl + "/resource/456";
        Map<String, Object> resourceData = Map.of(
            "id", 456,
            "title", "New Resource Title",
            "content", "Complete new content for the resource",
            "status", "active"
        );
        
        try {
            String jsonData = objectMapper.writeValueAsString(resourceData);
            EasyHttpPut4j.doRequestDefault(
                url,
                jsonData,
                "application/json",
                response -> System.out.println("Replace Resource Response: " + response)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}