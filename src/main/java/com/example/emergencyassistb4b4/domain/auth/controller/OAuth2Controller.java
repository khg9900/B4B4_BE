package com.example.emergencyassistb4b4.domain.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/auth")
public class OAuth2Controller {
    @GetMapping("/{provider}")
    public void redirectToProvider(@PathVariable String provider, HttpServletResponse response) throws IOException {
        String redirectUrl = "/oauth2/authorization/" + provider;
        response.sendRedirect(redirectUrl);
    }

    // OAuth2 로그인 성공 시 호출되는 핸들러 예시
    @GetMapping("/oauth2/success")
    public void oauth2LoginSuccess(
        @RequestParam String accessToken,
        @RequestParam String refreshToken,
        HttpServletResponse response) throws IOException {

        // WebView 방식에서 최종 리디렉션 주소
        String redirectUrl = "http://10.0.2.2:8080/oauth2/success?accessToken=" + accessToken + "&refreshToken=" + refreshToken;

        // String redirectUrl = "http://54.180.32.246:8080/oauth2/success?accessToken=" + accessToken + "&refreshToken=" + refreshToken;

        response.sendRedirect(redirectUrl);
    }

}