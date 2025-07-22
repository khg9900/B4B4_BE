package com.example.emergencyassistb4b4.domain.location.service;

import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.example.emergencyassistb4b4.domain.location.dto.response.DisasterReportMapper;
import com.example.emergencyassistb4b4.domain.location.dto.response.DisasterReportSimpleDto;
import com.example.emergencyassistb4b4.domain.location.dto.response.DisasterSummaryDto;
import com.example.emergencyassistb4b4.domain.location.dto.response.ShelterResponseDto;
import com.example.emergencyassistb4b4.domain.location.util.KakaoApiUtils;
import com.example.emergencyassistb4b4.domain.report.repository.ReportRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KakaoMapService {

    @Value("${kakao.api.key}")
    private String restApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ReportRepository reportRepository;

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

    public List<DisasterSummaryDto> getDisasterSummary(
            double latitude,
            double longitude,
            int radiusMeter,
            long secondsAgo
    ) {
        LocalDateTime fromTime = LocalDateTime.now().minusSeconds(secondsAgo);

        List<Object[]> rawReports = reportRepository.findNearbyDisasterReportsRaw(
                longitude, latitude, radiusMeter, fromTime
        );

        // ✅ 매핑 책임을 DisasterReportMapper로 위임
        List<DisasterReportSimpleDto> reports = DisasterReportMapper.map(rawReports);

        // disasterType_status 기준으로 그룹핑
        Map<String, List<DisasterReportSimpleDto>> grouped = reports.stream()
                .collect(Collectors.groupingBy(r -> r.getDisasterType().name() + "_" + r.getStatus().name()));

        return grouped.values().stream()
                .map(this::calculateMedian)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public DisasterSummaryDto calculateMedian(List<DisasterReportSimpleDto> reports) {
        if (reports.isEmpty()) return null;

        List<Double> latitudes = reports.stream()
                .map(DisasterReportSimpleDto::getLatitude)
                .sorted()
                .toList();

        List<Double> longitudes = reports.stream()
                .map(DisasterReportSimpleDto::getLongitude)
                .sorted()
                .toList();

        int size = reports.size();
        double medianLat = (size % 2 == 1) ?
                latitudes.get(size / 2) :
                (latitudes.get(size / 2 - 1) + latitudes.get(size / 2)) / 2.0;

        double medianLng = (size % 2 == 1) ?
                longitudes.get(size / 2) :
                (longitudes.get(size / 2 - 1) + longitudes.get(size / 2)) / 2.0;

        DisasterReportSimpleDto sample = reports.get(0);

        return new DisasterSummaryDto(
                sample.getDisasterType(),
                sample.getStatus(),
                size,
                medianLat,
                medianLng
        );
    }
}
