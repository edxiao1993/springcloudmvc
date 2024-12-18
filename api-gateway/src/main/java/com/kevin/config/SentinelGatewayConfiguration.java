package com.kevin.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kevin Chien
 * @version 0.1
 * @date 2024/12/18 23:16
 */
@Configuration
public class SentinelGatewayConfiguration {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("limit_route", r -> r
                        .path("/api/**")
                        .filters(f -> f
                                .requestRateLimiter()
                                .configure(c -> c
                                        .setKeyResolver(exchange -> Mono.just(exchange.getRequest().getPath().toString()))
                                )
                        )
                        .uri("http://your-backend-service")
                )
                .build();
    }

    @PostConstruct
    public void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();

        // Define rate limiting rule
        rules.add(new GatewayFlowRule("limit_route")
                .setCount(100)  // 100 requests
                .setIntervalSec(1)  // per second
                .setGrade(RuleConstant.FLOW_GRADE_QPS)  // QPS (Queries Per Second) mode
        );

        // Load the rules
        GatewayRuleManager.loadRules(rules);
    }

    @Bean
    public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler() {
            @Override
            public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
                // Custom block handler
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);

                String msg = "Too many requests, please try again later.";
                DataBuffer buffer = exchange.getResponse().bufferFactory()
                        .wrap(msg.getBytes(StandardCharsets.UTF_8));

                return exchange.getResponse().writeWith(Mono.just(buffer));
            }
        };
    }
}