package com.example.emergencyassistb4b4.global.security.config;

import com.example.emergencyassistb4b4.global.security.jwt.JwtTokenAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import com.example.emergencyassistb4b4.global.security.handler.CustomAccessDeniedHandler;
import com.example.emergencyassistb4b4.global.security.handler.CustomAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (JWT는 stateless하므로 필요 없음)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 비활성화

                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 허용할 경로
                        .requestMatchers(
                                "/auth/signup",
                                "/auth/login",
                                "/auth/reissue",
                                "/error/**", // Spring 기본 에러 핸들링
                                "/tracking/**", // 위치 추적 및 WebSocket 핸드쉐이크용
                                "/location-tracking/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**" // swagger 문서 접근 허용(API 테스트 편의성 목적)
                        ).permitAll()


                        // 특정 역할만 접근 가능한 경로 설정 예시 (필요시 추가)
                        //.requestMatchers("/admin/**").hasRole("ADMIN")

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // JWT 인증 필터 등록
                .addFilterAfter(
                        jwtTokenAuthenticationFilter,
                        ExceptionTranslationFilter.class
                )
                .exceptionHandling(exception -> exception // 예외 처리 설정
                        .authenticationEntryPoint(customAuthenticationEntryPoint) // 로그인x 일 경우 발동
                        .accessDeniedHandler(customAccessDeniedHandler) // 로그인o, 권한x 일 경우 발동
                );

        return http.build();
    }
}
