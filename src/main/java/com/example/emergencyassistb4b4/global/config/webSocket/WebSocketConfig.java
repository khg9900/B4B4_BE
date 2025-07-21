package com.example.emergencyassistb4b4.global.config.webSocket;

import com.example.emergencyassistb4b4.domain.attendance.socket.controller.LocationTrackingWebSocketHandler;
import com.example.emergencyassistb4b4.domain.attendance.socket.handler.TrackingSocketHandler;
import com.example.emergencyassistb4b4.global.security.JwtUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final TrackingSocketHandler trackingSocketHandler;
    private final LocationTrackingWebSocketHandler locationTrackingWebSocketHandler;
    private final JwtUtils jwtUtils;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 백엔드 → 프론트 메시지 전송용
        registry.addHandler(trackingSocketHandler, "/tracking")
                .addInterceptors(new JwtHandshakeInterceptor(jwtUtils))
                .setAllowedOrigins("http://localhost:5501", "http://127.0.0.1:5501");

        // 프론트 → 백엔드 위치 전송용
        registry.addHandler(locationTrackingWebSocketHandler, "/location-tracking")
                .addInterceptors(new JwtHandshakeInterceptor(jwtUtils))
                .setAllowedOrigins("http://localhost:5501", "http://127.0.0.1:5501");
    }
}

