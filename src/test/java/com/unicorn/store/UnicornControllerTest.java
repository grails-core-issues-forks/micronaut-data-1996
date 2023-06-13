package com.unicorn.store;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.unicorn.store.model.Unicorn;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.function.aws.proxy.BinaryContentConfiguration;
import io.micronaut.function.aws.proxy.payload1.ApiGatewayProxyRequestEventFunction;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.MediaType;
import io.micronaut.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UnicornControllerTest {
    private static Context lambdaContext = new Context() {
        @Override
        public String getAwsRequestId() {
            return null;
        }

        @Override
        public String getLogGroupName() {
            return null;
        }

        @Override
        public String getLogStreamName() {
            return null;
        }

        @Override
        public String getFunctionName() {
            return null;
        }

        @Override
        public String getFunctionVersion() {
            return null;
        }

        @Override
        public String getInvokedFunctionArn() {
            return null;
        }

        @Override
        public CognitoIdentity getIdentity() {
            return null;
        }

        @Override
        public ClientContext getClientContext() {
            return null;
        }

        @Override
        public int getRemainingTimeInMillis() {
            return 0;
        }

        @Override
        public int getMemoryLimitInMB() {
            return 0;
        }

        @Override
        public LambdaLogger getLogger() {
            return null;
        }
    };

    @Test
    void unicornCrud() throws IOException {
        ApiGatewayProxyRequestEventFunction handler = new ApiGatewayProxyRequestEventFunction();
        Unicorn unicorn = pojo();
        JsonMapper objectMapper = handler.getApplicationContext().getBean(JsonMapper.class);
        handler.getApplicationContext().containsBean(BinaryContentConfiguration.class);
        APIGatewayProxyRequestEvent createRequest = create(unicorn, objectMapper);
        APIGatewayProxyResponseEvent response = handler.handleRequest(createRequest, lambdaContext);
        assertEquals(201, response.getStatusCode());
        List<String> paths = response.getMultiValueHeaders().get(HttpHeaders.LOCATION);
        assertNotNull(paths);
        assertTrue(CollectionUtils.isNotEmpty(paths));
        String path = paths.get(0);

        APIGatewayProxyRequestEvent getRequest = get(path);
        APIGatewayProxyResponseEvent getResponse = handler.handleRequest(getRequest, lambdaContext);
        assertEquals(200, getResponse.getStatusCode());

        unicorn.setId(path.substring("/unicorns/".length()));
        Unicorn received = objectMapper.readValue(getResponse.getBody(), Argument.of(Unicorn.class));
        assertEquals(unicorn.getId(), received.getId());
        assertEquals(unicorn.getAge(), received.getAge());
        assertEquals(unicorn.getName(), received.getName());
        assertEquals(unicorn.getType(), received.getType());
        assertEquals(unicorn.getSize(), received.getSize());

        APIGatewayProxyRequestEvent deleteRequest = delete(path);
        APIGatewayProxyResponseEvent deleteResponse = handler.handleRequest(deleteRequest, lambdaContext);
        assertEquals(204, deleteResponse.getStatusCode());

        getResponse = handler.handleRequest(getRequest, lambdaContext);
        assertEquals(404, getResponse.getStatusCode());
        assertTrue(getResponse.getBody().contains("Unicorn not found"));

        handler.close();
    }

    APIGatewayProxyRequestEvent create(Unicorn unicorn, JsonMapper objectMapper) throws IOException {
        APIGatewayProxyRequestEvent proxyRequest = new APIGatewayProxyRequestEvent();
        proxyRequest.setBody(sampleBody(unicorn, objectMapper));
        proxyRequest.setHeaders(Collections.singletonMap(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON));
        proxyRequest.setMultiValueHeaders(Collections.singletonMap(HttpHeaders.CONTENT_TYPE, Collections.singletonList(MediaType.APPLICATION_JSON)));
        proxyRequest.setPath("/unicorns");
        proxyRequest.setHttpMethod(HttpMethod.POST.toString());
        return proxyRequest;
    }

    APIGatewayProxyRequestEvent get(String path) {
        APIGatewayProxyRequestEvent proxyRequest = new APIGatewayProxyRequestEvent();
        proxyRequest.setHeaders(Collections.singletonMap(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON));
        proxyRequest.setMultiValueHeaders(Collections.singletonMap(HttpHeaders.ACCEPT, Collections.singletonList(MediaType.APPLICATION_JSON)));
        proxyRequest.setPath(path);
        proxyRequest.setHttpMethod(HttpMethod.GET.toString());
        return proxyRequest;
    }

    APIGatewayProxyRequestEvent delete(String path) {
        APIGatewayProxyRequestEvent proxyRequest = new APIGatewayProxyRequestEvent();
        proxyRequest.setPath(path);
        proxyRequest.setHeaders(Collections.singletonMap(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON));
        proxyRequest.setMultiValueHeaders(Collections.singletonMap(HttpHeaders.ACCEPT, Collections.singletonList(MediaType.APPLICATION_JSON)));
        proxyRequest.setHttpMethod(HttpMethod.DELETE.toString());
        return proxyRequest;
    }

    private Unicorn pojo() {
        Unicorn unicorn = new Unicorn();
        unicorn.setName("John");
        unicorn.setAge("8");
        unicorn.setSize("XL");
        unicorn.setType("White");
        return unicorn;
    }

    private String sampleBody(Unicorn unicorn, JsonMapper objectMapper) throws IOException {
        return new String(objectMapper.writeValueAsBytes(unicorn), StandardCharsets.UTF_8);
    }
}
