package io.github.lookoutldz.easyrequester;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import io.github.lookoutldz.easyrequester.requester4j.EasyHttpPost4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class JavaHttpPostTest {

    private static final String baseUrl = "http://localhost:58080/api/post";
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new KotlinModule.Builder().build());

    @Test
    public void contextLoads() {
        System.out.println("post context ok");
    }

    /**
     * simple post
     */
    @Test
    public void post() {
        String simplePostUrl = "http://localhost:58080/api/post/";
        EasyHttpPost4j.doRequestDefault(simplePostUrl, null, null, System.out::println);
    }

    @Test
    public void postUser() {
        String simplePostUrl = "http://localhost:58080/api/post/user";
        try {
            String jsonUser = objectMapper.writeValueAsString(new User(114514, "Dark"));
            System.out.println(jsonUser);
            EasyHttpPost4j.doRequestDefault(simplePostUrl, jsonUser, null, System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试JSON Content-Type接口
     */
    @Test
    public void testJsonPost() {
        String url = baseUrl + "/json";
        User user = new User(123, "JsonUser");
        try {
            String jsonBody = objectMapper.writeValueAsString(user);
            EasyHttpPost4j.doRequestDefault(
                url,
                jsonBody,
                "application/json",
                response -> System.out.println("JSON POST Response: " + response)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试XML Content-Type接口
     */
    @Test
    public void testXmlPost() {
        String url = baseUrl + "/xml";
        String xmlBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<user>\n" +
                "    <userId>456</userId>\n" +
                "    <name>XmlUser</name>\n" +
                "</user>";

        // both ok
        RequestBody requestBody = RequestBody.create(xmlBody, MediaType.get("text/xml"));
        
        EasyHttpPost4j.doRequestDefault(
            url,
            requestBody,
            null,
            response -> System.out.println("XML POST Response: " + response)
        );
    }

    /**
     * 测试Form URL Encoded Content-Type接口
     */
    @Test
    public void testFormPost() {
        String url = baseUrl + "/form";
        String formBody = "id=789&name=FormUser";
        
        EasyHttpPost4j.doRequestDefault(
            url,
            formBody,
            "application/x-www-form-urlencoded",
            response -> System.out.println("Form POST Response: " + response)
        );
    }

    /**
     * 测试Multipart Form Data Content-Type接口
     */
    @Test
    public void testMultipartPost() {
        String url = baseUrl + "/multipart";
        
        // 创建multipart body
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
        
        EasyHttpPost4j.doRequestDefault(
            url,
            multipartBody,
            null,
            response -> System.out.println("Multipart POST Response: " + response)
        );
    }

    /**
     * 测试Text Content-Type接口
     */
    @Test
    public void testTextPost() {
        String url = baseUrl + "/text";
        String textContent = "This is a plain text message for testing";
        
        EasyHttpPost4j.doRequestDefault(
            url,
            textContent,
            "text/plain",
            response -> System.out.println("Text POST Response: " + response)
        );
    }

    /**
     * 测试接受任意Content-Type的接口
     */
    @Test
    public void testAnyPost() {
        String url = baseUrl + "/any";
        String anyContent = "Any content type test";
        
        EasyHttpPost4j.doRequestDefault(
            url,
            anyContent,
            null,
            response -> System.out.println("Any POST Response: " + response)
        );
    }

    /**
     * 测试使用Builder模式的JSON请求
     */
    @Test
    public void testJsonPostWithBuilder() {
        String url = baseUrl + "/json";
        User user = new User(555, "BuilderUser");
        try {
            String jsonBody = objectMapper.writeValueAsString(user);
            
            new EasyHttpPost4j.Builder<>(String.class)
                    .setUrl(url)
                    .setBody(jsonBody)
                    .setContentType("application/json")
                    .onSuccess(response -> System.out.println("Builder JSON POST Response: " + response))
                    .onException((throwable, request) -> System.out.println("Builder JSON POST Error: " + throwable.getMessage()))
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
    public void testPostWithHeaders() {
        String url = baseUrl + "/json";
        User user = new User(777, "HeaderUser");
        try {
            String jsonBody = objectMapper.writeValueAsString(user);
            Map<String, String> headers = Map.of(
                "Authorization", "Bearer test-token",
                "X-Custom-Header", "custom-value"
            );
            
            new EasyHttpPost4j.Builder<>(String.class)
                    .setUrl(url)
                    .setBody(jsonBody)
                    .setContentType("application/json")
                    .setHeaders(headers)
                    .onSuccess(response -> System.out.println("POST with Headers Response: " + response))
                    .build()
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}