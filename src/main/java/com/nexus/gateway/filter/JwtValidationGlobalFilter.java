package com.nexus.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.common.core.ApiError;
import com.nexus.common.core.ApiResponse;
import com.nexus.common.security.JwtTokenProvider;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtValidationGlobalFilter implements GlobalFilter, Ordered {

    // Same matcher semantics as the `Path=/api/v1/products/**,/api/v1/categories/**`
    // gateway route predicate in application.yml, so the public-path check here agrees
    // with what actually gets routed to catalog-service. Segment-aware — unlike a bare
    // String#startsWith, "/api/v1/products/**" will NOT match "/api/v1/products-fake".
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String PRODUCTS_PATTERN = "/api/v1/products/**";
    private static final String CATEGORIES_PATTERN = "/api/v1/categories/**";
    private static final String AUCTIONS_PATTERN = "/api/v1/auctions/**";

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    public JwtValidationGlobalFilter(JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (isPublic(exchange.getRequest())) {
            return chain.filter(exchange);
        }

        String token = extractToken(exchange.getRequest());
        if (token == null || !jwtTokenProvider.isValid(token)) {
            return unauthorized(exchange);
        }

        return chain.filter(exchange);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        ApiError error = new ApiError("UNAUTHENTICATED", "Authentication required", List.of());
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(ApiResponse.error(error));
        } catch (JsonProcessingException e) {
            bytes = "{}".getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublic(ServerHttpRequest request) {
        String path = request.getURI().getPath();

        if (path.equals("/api/v1/users/register") || path.equals("/api/v1/auth/login")) {
            return true;
        }
        if (path.equals("/actuator") || path.startsWith("/actuator/")) {
            return true;
        }
        if (HttpMethod.GET.equals(request.getMethod())
                && (PATH_MATCHER.match(PRODUCTS_PATTERN, path) || PATH_MATCHER.match(CATEGORIES_PATTERN, path)
                    || PATH_MATCHER.match(AUCTIONS_PATTERN, path))) {
            return true;
        }
        return false;
    }

    private String extractToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
