package com.kevin.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author Kevin Chien
 * @version 0.1
 * @date 2024/12/16 18:51
 */
@Component
public class GlobalFilterAll implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(GlobalFilterAll.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        logger.info("hey there, I am helping you to filter something");
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
