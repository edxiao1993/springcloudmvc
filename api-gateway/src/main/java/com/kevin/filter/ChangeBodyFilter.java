package com.kevin.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kevin Chien
 * @version 0.1
 * @date 2024/12/16 22:40
 */
@Component
public class ChangeBodyFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(GlobalFilterAll.class);

    private final ObjectMapper objectMapper;

    @Autowired
    public ChangeBodyFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (exchange.getRequest().getURI().toString().contains("user")) {
            logger.info("the uri contains user, let it go~");
            chain.filter(exchange);
        }

        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    String bodyString = new String(bytes, StandardCharsets.UTF_8);
                    try {
                        Map<String, Object> bodyMap;
                        if (!bodyString.isEmpty()) {
                            bodyMap = objectMapper.readValue(bodyString, Map.class);
                        } else {
                            bodyMap = new HashMap<>();
                        }

                        bodyMap.put("addedField", "addedValueFromGlobalFilter");
                        String modifiedBody = objectMapper.writeValueAsString(bodyMap);
                        byte[] modifiedBytes = modifiedBody.getBytes(StandardCharsets.UTF_8);
                        DataBuffer modifiedDataBuffer = exchange.getResponse().bufferFactory().wrap(modifiedBytes);

                        ServerHttpRequest modifiedRequest = new ServerHttpRequestDecorator(exchange.getRequest()){
                            @Override
                            public Flux<DataBuffer> getBody() {
                                return Flux.just(modifiedDataBuffer);
                            }

                            @Override
                            public HttpHeaders getHeaders() {
                                HttpHeaders headers = new HttpHeaders();
                                headers.putAll(super.getHeaders());
                                headers.setContentLength(modifiedBytes.length);
                                return headers;
                            }
                        };

                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Mono.error(e);
                    }
                });
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
