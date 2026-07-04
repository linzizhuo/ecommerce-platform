package com.cloudmall.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Gateway 全局 JWT 鉴权过滤器
 */
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String SECRET = "CloudMall2026SecretKeyForJWTTokenSigning!";
    private static final SecretKey KEY = new SecretKeySpec(
            SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    /** 公开路径，无需token */
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/user/login", "/api/user/register",
            "/api/admin/login",
            "/api/merchant/login",
            "/api/product", "/api/category",
            "/api/review/product",
            "/api/search",
            "/api/notice/active",
            "/api/seckill/sessions",
            "/api/presale/list",
            "/api/combo",
            "/api/point/levels",
            "/api/config",
            "/api/venue",
            "/api/dict",
            "/doc.html", "/v3/api-docs", "/swagger", "/webjars"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 公开路径直接放行
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // OPTIONS 预检请求放行
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequest().getMethod().name())) {
            return chain.filter(exchange);
        }

        // 静态资源放行
        if (path.startsWith("/static") || path.startsWith("/css") || path.startsWith("/js")) {
            return chain.filter(exchange);
        }

        // 验证 JWT
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            String token = authHeader.substring(7);
            Claims claims = Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // 将 userId 放入请求头传递给下游服务
            String userId = claims.getSubject();
            exchange = exchange.mutate()
                    .request(r -> r.header("X-User-Id", userId != null ? userId : ""))
                    .build();
        } catch (Exception e) {
            log.warn("JWT verification failed: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
