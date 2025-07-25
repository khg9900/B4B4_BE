package com.example.emergencyassistb4b4.global.config.webSocket;

import com.example.emergencyassistb4b4.global.security.jwt.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Slf4j
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtils jwtUtils;

    public JwtHandshakeInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            URI uri = request.getURI();
            MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
            String token = queryParams.getFirst("token");

            if (token == null || token.isEmpty()) {
                log.warn("WebSocket 연결 거부: token 파라미터가 없음 또는 비어있음");
                return false;
            }

            // 토큰 일부 마스킹 (앞 6글자 + 뒤 6글자)
            String maskedToken = token.length() > 12 ? token.substring(0, 6) + "..." + token.substring(token.length() - 6) : token;
            log.info("WebSocket 핸드셰이크 token: {}", maskedToken);

            if (jwtUtils.validateToken(token)) {
                Authentication authentication = jwtUtils.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                attributes.put("authentication", authentication);
                Long userId = jwtUtils.getUserId(token);
                attributes.put("userId", userId);
                attributes.put("token", token);  // 추가

                log.info("WebSocket 연결 허용 - 사용자 ID: {}", userId);
                return true;
            } else {
                log.warn("WebSocket 연결 거부: 토큰 검증 실패");
            }
        } catch (Exception e) {
            log.error("WebSocket 핸드셰이크 중 예외 발생", e);
        }
        return false;
    }


    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 필요 시 후처리
    }
}
