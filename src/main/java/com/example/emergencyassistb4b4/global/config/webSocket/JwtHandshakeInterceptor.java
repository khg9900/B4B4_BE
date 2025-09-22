package com.example.emergencyassistb4b4.global.config.webSocket;

import com.example.emergencyassistb4b4.global.security.jwt.JwtUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;

    // WebSocket 연결 전에 JWT 토큰을 검증하고 인증 정보를 설정
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            URI uri = request.getURI();
            MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
            String token = queryParams.getFirst("token");

            if (token == null || token.isEmpty()) {
                log.warn("WebSocket 연결 거부: token 파라미터 없음 또는 비어있음");
                return false;
            }

            if (jwtUtils.validateToken(token)) {
                Authentication authentication = jwtUtils.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                attributes.put("authentication", authentication);
                Long userId = jwtUtils.getUserId(token);
                attributes.put("userId", userId);
                attributes.put("token", token);

                return true;
            } else {
                log.warn("WebSocket 연결 거부: 토큰 검증 실패");
            }
        } catch (Exception e) {
            log.error("WebSocket 핸드셰이크 중 예외 발생", e);
        }
        return false;
    }

    // WebSocket 연결 후 처리 (필요 시 구현)
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 후처리 목적 메서드
    }
}
