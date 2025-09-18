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
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 허용할 경로
                        .requestMatchers(
                                "/auth/signup",
                                "/auth/login",
                                "/auth/reissue",
                                "/error/**",
                                "/tracking/**",
                                "/location-tracking/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        )
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .addFilterAfter(
                        jwtTokenAuthenticationFilter,
                        ExceptionTranslationFilter.class
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                );

        return http.build();
    }
}
