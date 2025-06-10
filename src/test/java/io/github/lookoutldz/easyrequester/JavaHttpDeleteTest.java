package io.github.lookoutldz.easyrequester;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import io.github.lookoutldz.easyrequester.requester4j.EasyHttpDelete4j;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class JavaHttpDeleteTest {

    @Test
    public void contextLoads() {
        System.out.println("delete context ok");
    }

    // DELETE请求通常用于删除资源
    private static final String baseUrl = "http://127.0.0.1:58080/api/delete";
    private static final String userDeleteUrl = "http://127.0.0.1:58080/api/delete/user/123";
    private static final Map<String, String> params = Map.of("force", "true");
    private static final Map<String, String> headers = Map.of("Authorization", "Bearer token123");
    private static final Map<String, String> cookies = Map.of("sessionId", "abc123");

    @Test
    public void testDoRequestDefault() {
        // 测试最简单的默认DELETE请求方式
        EasyHttpDelete4j.doRequestDefault(userDeleteUrl, null, null, result -> {
            // it is default to string
            System.out.println("01. DELETE ok - " + result);
        });
    }

    @Test
    public void testDoRequestWithType() {
        // 测试指定返回类型的DELETE请求
        EasyHttpDelete4j.doRequest(
            new TypeReference<ResponseBody<Object>>() {},
            userDeleteUrl,
            null,
            null,
            responseBody -> System.out.println("02. DELETE ok - " + (responseBody != null ? responseBody.getData() : null))
        );
    }

    @Test
    public void testDoRequestWithAllParams() {
        // 测试带有完整参数的DELETE请求
        new EasyHttpDelete4j.Builder<>(new TypeReference<ResponseBody<Object>>() {})
                .setUrl(baseUrl)
                .setParams(params)
                .setHeaders(headers)
                .setCookies(cookies)
                .setOkHttpClient(new OkHttpClient())
                .setObjectMapper(new ObjectMapper().registerModule(new KotlinModule.Builder().build()))
                .onSuccess(responseBody -> System.out.println("03. DELETE ok - " + (responseBody != null ? responseBody.getData() : null)))
                .build()
                .execute();
    }

    @Test
    public void testDoRequestWithExceptionHandler() {
        // 测试自定义异常处理器
        new EasyHttpDelete4j.Builder<>(new TypeReference<ResponseBody<Object>>() {})
                .setUrl(userDeleteUrl)
                .setObjectMapper(new ObjectMapper())  // Exception here: this is not the correct ObjectMapper for Kotlin Data Class
                .onException((throwable, request) -> {
                    System.out.println("04. My ExceptionHandler - [" + request.method() + "]" + request.url() + " cause " + (throwable != null ? throwable.getMessage() : "null"));
                })
                .onSuccess(responseBody -> System.out.println("04. DELETE ok - " + (responseBody != null ? responseBody.getData() : null)))
                .build()
                .execute();
    }

    @Test
    public void testDoRequestRaw() {
        // 测试处理原始响应
        EasyHttpDelete4j.doRequestRaw(userDeleteUrl, null, null, response -> {
            System.out.println("05. DELETE ok - " + response.isSuccessful() + " - " + response.code() + " - " + response.message());
        });
    }

    @Test
    public void testBuilderPattern() {
        // 测试使用Builder模式构建DELETE请求
        new EasyHttpDelete4j.Builder<>(new TypeReference<ResponseBody<Object>>() {})
                .setUrl(baseUrl)
                .setParams(params)
                .setHeaders(headers)
                .setCookies(cookies)
                .setOkHttpClient(new OkHttpClient())
                .setObjectMapper(new ObjectMapper().registerModule(new KotlinModule.Builder().build()))
                .onSuccess(responseBody -> System.out.println("06. DELETE ok - " + (responseBody != null ? responseBody.getData() : null)))
                .onException((throwable, request) -> {
                    System.out.println("06. My ExceptionHandler - [" + request.method() + "]" + request.url() + " cause " + (throwable != null ? throwable.getMessage() : "null"));
                })
                .build()
                .execute();  // don't forget to execute
    }

    @Test
    public void testBuilderWithTypeReference() {
        // 测试使用TypeReference指定类型的Builder模式
        new EasyHttpDelete4j.Builder<>(new TypeReference<ResponseBody<User>>() {})
                .setUrl(userDeleteUrl)
                .onSuccess(responseBody -> {
                    if (responseBody != null && responseBody.getData() != null) {
                        System.out.println("07. DELETE ok - " + responseBody.getData().getName());
                    }
                })
                .build()
                .execute();
    }

    @Test
    public void testDeleteWithConfirmation() {
        // 测试需要确认的删除操作
        String confirmDeleteUrl = baseUrl + "/confirm";
        Map<String, String> confirmParams = Map.of("confirm", "yes", "reason", "test");
        Map<String, String> confirmHeaders = Map.of("X-Confirm-Delete", "true");
        
        new EasyHttpDelete4j.Builder<>(new TypeReference<ResponseBody<String>>() {})
                .setUrl(confirmDeleteUrl)
                .setParams(confirmParams)
                .setHeaders(confirmHeaders)
                .onSuccess(responseBody -> {
                    if (responseBody != null) {
                        System.out.println("08. Confirmed DELETE ok - " + responseBody.getStatusMessage());
                    }
                })
                .build()
                .execute();
    }

    @Test
    public void testBatchDelete() {
        // 测试批量删除
        String batchDeleteUrl = baseUrl + "/batch";
        Map<String, String> batchParams = Map.of("ids", "1,2,3,4,5");
        
        EasyHttpDelete4j.doRequestDefault(
            batchDeleteUrl,
            batchParams,
            null,
            response -> System.out.println("09. Batch DELETE ok - " + response)
        );
    }
}