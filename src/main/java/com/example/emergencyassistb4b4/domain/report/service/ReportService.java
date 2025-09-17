package com.example.emergencyassistb4b4.domain.report.service;

import com.example.emergencyassistb4b4.domain.report.dto.*;
import com.example.emergencyassistb4b4.global.S3.S3Uploader;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.kafka.dto.DisasterReportedEvent;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.example.emergencyassistb4b4.domain.report.domain.Report;
import com.example.emergencyassistb4b4.domain.report.enums.ReportStatus;
import com.example.emergencyassistb4b4.domain.report.kafka.producer.DisasterReportedEventProducer;
import com.example.emergencyassistb4b4.domain.report.repository.ReportRepository;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.domain.UserRole;
import com.example.emergencyassistb4b4.domain.user.repository.UserRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.emergencyassistb4b4.global.status.ErrorStatus.FORBIDDEN;
import static com.example.emergencyassistb4b4.global.status.ErrorStatus.REPORT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final DisasterReportedEventProducer producer;
    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;

    // (사용자) 재난 신고 기능
    @Transactional
    public ReportResponseDto disasterReport(ReportRequestDto requestDto, User reporter, MultipartFile image, MultipartFile video) throws IOException {

        String imageUrl = null;
        String videoUrl = null;

        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = s3Uploader.uploadFile(image, "images");
            } catch (IOException e) {
                throw new ApiException(ErrorStatus.S3_UPLOAD_ERROR); // 커스텀 예외
            }
        }

        if (video != null && !video.isEmpty()) {
            try {
                videoUrl = s3Uploader.uploadFile(video, "videos");
            } catch (IOException e) {
                throw new ApiException(ErrorStatus.S3_UPLOAD_ERROR); // 커스텀 예외
            }
        }

        double latitude = requestDto.getLatitude();
        double longitude = requestDto.getLongitude();
        String province = requestDto.getProvince();
        String rawCity = requestDto.getCity();

// 빈문자/공백 → null
        String normCity = (rawCity != null && !rawCity.isBlank()) ? rawCity.trim() : null;

        // 위도, 경도 -> point
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point location = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        User responder;
        boolean provinceOnly = "세종특별자치시".equals(province) || normCity == null;

        if (provinceOnly) {
            responder = userRepository.findFirstByProvinceAndUserRole(province, UserRole.GOV)
                    .orElseThrow(() -> new ApiException(ErrorStatus.GOV_NOT_FOUND));
        } else {
            responder = userRepository.findFirstByProvinceAndCityAndUserRole(province, normCity, UserRole.GOV)
                    .orElseThrow(() -> new ApiException(ErrorStatus.GOV_NOT_FOUND));
        }

        // 신고 저장
        Report report = Report.builder()
                .reporter(reporter)
                .disasterType(requestDto.getDisasterType())
                .description(requestDto.getDescription())
                .imageUrl(imageUrl)
                .videoUrl(videoUrl)
                .status(ReportStatus.PENDING)
                .province(province) // 예시: 위치 서비스로 가져온 값
                .city(normCity)
                .location(location)
                .responder(responder)
                .build();

        Report savedReport = reportRepository.save(report);

        // kafka 메세지 발행
        DisasterReportedEvent event = DisasterReportedEvent.from(savedReport);
        producer.sendDisasterReportedEvent(event);

        // Dto 반환
        return ReportResponseDto.from(savedReport);
    }


    // (공공기관) 신고 내역 조회 기능
    @Transactional(readOnly = true)
    public List<ReportResponseDto> getReportList(User responder) {

        List<Report> reports = reportRepository.findAllByResponder(responder);

        return reports.stream()
                .map(ReportResponseDto::from)
                .collect(Collectors.toList());
    }


    /** 공공기관 단건 상태변경 */
    @PreAuthorize("hasRole('GOV')")
    @Transactional
    public ReportStatusResponseDto changeReportStatus(
            Long publicId, //공공기관 Id
            Long reportId,
            ReportStatus newStatus){

        /* 공공기관인지 검증 */
        userRepository.findById(publicId).orElseThrow(() -> new ApiException(FORBIDDEN)); //권한

        // Report 조회
        Report r = reportRepository.findById(reportId).orElseThrow(
                ()->new ApiException(REPORT_NOT_FOUND));

        //상태 변경
        r.updateStatus(newStatus);
        return new ReportStatusResponseDto(reportId,newStatus);
    }

    // 주변 신고 목록 조회
    @PreAuthorize("hasRole('GOV')")
    @Transactional(readOnly = true)
    public Slice<ReportDto> getNearbyReports(String si, String gu, ReportStatus status, Pageable pageable) {

        return reportRepository.findNearby(si, gu, status, pageable).map(ReportDto::of);
    }

    //내 신고 목록 조회 (신고한 유저의 목록)
    @Transactional(readOnly = true)
    public Slice<ReportDto> getMyReports(
            Long userId, ReportStatus status, LocalDateTime start,
            LocalDateTime end, Pageable pageable){

        return reportRepository.findByReporter(userId, status, start, end, pageable)
                .map(ReportDto::of);
    }

    //  Cursor
    @Transactional(readOnly = true)
    public CursorResponse<ReportDto> getNearbyReportsByCursor(ReportCursorRequest req) {
        int size = req.effectivePageSize();
        List<Report> rows = reportRepository.findNearbyByCursor(
                req.province(), req.city(), req.status(),
                req.lastCreatedAt(), req.lastId(),
                size + 1
        );

        List<ReportDto> content = rows.stream()
                .limit(size)
                .map(ReportDto::of)
                .toList();

        // ReportDto의 getter를 그대로 사용
        return CursorResponse.of(
                content, size,
                ReportDto::getCreatedAt,
                ReportDto::getId
        );
    }

    @Transactional(readOnly = true)
    public CursorResponse<ReportDto> getMyReportsByCursor(Long userId, String sortOrder, ReportCursorRequest req) {
        int size = req.effectivePageSize();
        boolean isDesc = !"ASC".equalsIgnoreCase(sortOrder);

        List<Report> rows = reportRepository.findByReporterByCursor(
                userId,
                req.status(),
                req.startDate(),
                req.endDate(),
                req.lastCreatedAt(),
                req.lastId(),
                size + 1,
                isDesc
        );

        List<ReportDto> content = rows.stream()
                .limit(size)
                .map(ReportDto::of)
                .toList();

        return CursorResponse.of(
                content, size,
                ReportDto::getCreatedAt,
                ReportDto::getId
        );
    }

    @Transactional(readOnly = true)
    public TodayReportStatusCounts getReportsSummary(Long publicId) {
        return reportRepository.getReportsSummary(publicId);
    }

}