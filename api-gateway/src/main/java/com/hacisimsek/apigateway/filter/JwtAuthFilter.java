package com.hacisimsek.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();

            String authHeader =
                    request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorized(
                        exchange.getResponse(),
                        "Missing or invalid Authorization header"
                );
            }

            String token = authHeader.substring(7);

            try {

                Claims claims = parseToken(token);

                ServerHttpRequest mutatedRequest =
                        request.mutate()
                                .header("X-User-Id", claims.getSubject())
                                .header("X-User-Email",
                                        claims.get("email", String.class))
                                .header("X-User-Role",
                                        claims.get("role", String.class))
                                .build();

                return chain.filter(
                        exchange.mutate()
                                .request(mutatedRequest)
                                .build()
                );

            } catch (ExpiredJwtException e) {

                log.warn("JWT expired for request: {}", request.getURI());

                return unauthorized(
                        exchange.getResponse(),
                        "JWT token has expired"
                );

            } catch (MalformedJwtException | IllegalArgumentException e) {

                log.warn("Invalid JWT for request: {}", request.getURI());

                return unauthorized(
                        exchange.getResponse(),
                        "Invalid JWT token"
                );

            } catch (Exception e) {

                log.error("JWT validation error: {}", e.getMessage());

                return unauthorized(
                        exchange.getResponse(),
                        "JWT validation failed"
                );
            }
        };
    }

    private Claims parseToken(String token) {

        byte[] keyBytes = hexStringToByteArray(jwtSecret);

        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private byte[] hexStringToByteArray(String hex) {

        int len = hex.length();

        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {

            data[i / 2] = (byte)
                    ((Character.digit(hex.charAt(i), 16) << 4)
                            + Character.digit(hex.charAt(i + 1), 16));
        }

        return data;
    }

    private Mono<Void> unauthorized(
            ServerHttpResponse response,
            String message
    ) {

        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        response.getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {"success":false,"message":"%s"}
                """.formatted(message);

        DataBuffer buffer =
                response.bufferFactory()
                        .wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
    }
}