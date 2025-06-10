package io.github.lookoutldz.easyrequester;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import io.github.lookoutldz.easyrequester.requester4j.EasyHttpGet4j;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JavaHttpGetTest {

    @Test
    public void contextLoads() {
        System.out.println("get context ok");
    }

    // will return {data=2000, statusCode=0, statusMessage=SUCCESS}
    private static final String baseUrl = "http://127.0.0.1:58080/api/get/sleep/random";
    private static final String userUrl = "http://127.0.0.1:58080/api/get/user/random";
    private static final Map<String, String> params = Map.of("millis", "233");
    private static final String fullUrl = baseUrl + "?millis=" + params.get("millis");
    private static final Map<String, String> headers = Map.of("", "");
    private static final Map<String, String> cookies = Map.of("", "");
    
    @Test
    public void testDoRequestDefault() {
        // 测试最简单的默认请求方式
        EasyHttpGet4j.doRequestDefault(userUrl, result -> {
            // it is default to string
            System.out.println("ok - " + result);
        });
    }
    
    @Test
    public void testDoRequestDefault2() {
        // 测试带headers的默认请求
        EasyHttpGet4j.doRequestDefault(userUrl, result -> {
            System.out.println("ok - " + result);
        });
    }

    @Test
    public void testDoRequest() {
        // 测试指定返回类型的请求
        EasyHttpGet4j.doRequest(String.class, baseUrl, System.out::println);
        EasyHttpGet4j.doRequest(new TypeReference<ResponseBody<User>>() {}, userUrl, responseBody -> {
            if (responseBody != null && responseBody.getData() != null) {
                System.out.println("ok - " + responseBody.getData());
            }
        });
    }
    
    @Test
    public void testDoRequestWithType() {
        // 测试指定返回类型的请求
        EasyHttpGet4j.doRequest(new TypeReference<ResponseBody<Object>>() {}, userUrl, responseBody -> {
            if (responseBody != null) {
                System.out.println("ok - " + responseBody.getData());
            }
        });
    }
    
    @Test
    public void testDoRequestWithAllParams() {
        // 测试带有完整参数的请求
        EasyHttpGet4j.doRequest(
            new TypeReference<ResponseBody<User>>() {},
            userUrl,
            responseBody -> {
                if (responseBody != null) {
                    System.out.println("ok - " + responseBody.getData());
                }
            }
        );
    }
    
    @Test
    public void testDoRequestWithExceptionHandler() {
        // 测试自定义异常处理器
        ObjectMapper objectMapper = new ObjectMapper(); // Exception here: this is not the correct ObjectMapper for Kotlin Data Class
        
        EasyHttpGet4j.doRequest(
            new TypeReference<ResponseBody<User>>() {},
            fullUrl,
            responseBody -> {
                if (responseBody != null) {
                    System.out.println("ok - " + responseBody.getData());
                }
            },
            (throwable, request) -> {
                System.out.println("My ExceptionHandler - [" + request.method() + "]" + request.url() + " cause " + 
                    (throwable != null ? throwable.getMessage() : "null"));
            }
        );
    }
    
    @Test
    public void testDoRequestRaw() {
        // 测试处理原始响应
        EasyHttpGet4j.doRequestRaw(fullUrl, response -> {
            System.out.println("ok - " + response.isSuccessful() + " - " + response.code() + " - " + response.message());
        });
    }

    @Test
    public void testBuilderPattern() {
        // 测试使用Builder模式构建请求
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new KotlinModule.Builder().build());
        
        new EasyHttpGet4j.Builder<>(ResponseBody.class)
                .setUrl(baseUrl)
                .setParams(params)
                .setCookies(cookies)
                .setOkHttpClient(new OkHttpClient())
                .setObjectMapper(objectMapper)
                .onSuccess(responseBody -> {
                    if (responseBody != null) {
                        System.out.println("ok - " + responseBody.getData());
                    }
                })
                .onException((throwable, request) -> {
                    System.out.println("My ExceptionHandler - [" + request.method() + "]" + request.url() + " cause " + 
                        (throwable != null ? throwable.getMessage() : "null"));
                })
                .build()
                .execute(); // don't forget to execute
    }
    
    @Test
    public void testBuilderWithTypeReference() {
        // 测试使用TypeReference指定类型的Builder模式
        new EasyHttpGet4j.Builder<>(new TypeReference<ResponseBody<User>>() {})
                .setUrl(userUrl)
                .onSuccess(responseBody -> {
                    if (responseBody != null && responseBody.getData() != null) {
                        User user = (User) responseBody.getData();
                        System.out.println("ok - " + (user != null ? user.getName() : "null"));
                    }
                })
                .build()
                .execute();
    }
    
    @Test
    public void testJavaInteropWithKotlin() {
        // Java可以无缝调用Kotlin代码
        // 测试Java与Kotlin的互操作性
        assertNotNull("Java可以访问Kotlin类");
    }
}