package com.example.emergencyassistb4b4.domain.location.service;

import com.example.emergencyassistb4b4.domain.location.dto.response.ShelterResponseDto;
import com.example.emergencyassistb4b4.domain.location.util.KakaoApiUtils;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShelterService {

    @Value("${kakao.api.key}")
    private String restApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // 카카오 API를 호출하여 반경 내 대피소(PO3: 치안기관) 조회
    public List<ShelterResponseDto> searchShelters(double latitude, double longitude, double radiusMeter) {
        String categoryCode = "PO3"; // 치안기관
        String url = KakaoApiUtils.buildCategorySearchUrl(categoryCode, longitude, latitude, radiusMeter);

        HttpHeaders headers = KakaoApiUtils.createAuthHeader(restApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new ApiException(ErrorStatus.KAKAO_API_REQUEST_FAILED);
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode documents = root.path("documents");

            List<ShelterResponseDto> shelters = new ArrayList<>();
            for (int i = 0; i < Math.min(3, documents.size()); i++) {
                shelters.add(ShelterResponseDto.from(documents.get(i)));
            }

            return shelters;

        } catch (JsonProcessingException e) {
            throw new ApiException(ErrorStatus.KAKAO_API_RESPONSE_PARSE_FAILED);
        } catch (RestClientException e) {
            throw new ApiException(ErrorStatus.KAKAO_API_RESPONSE_STATUS_ERROR);
        }
    }
}
