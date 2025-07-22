package com.example.emergencyassistb4b4.report.repository;

import com.example.emergencyassistb4b4.domain.report.enums.DisasterType;
import com.example.emergencyassistb4b4.domain.report.domain.Report;
import com.example.emergencyassistb4b4.domain.report.dto.ReportDto;
import com.example.emergencyassistb4b4.domain.report.dto.ReportStatusResponseDto;
import com.example.emergencyassistb4b4.domain.report.enums.ReportStatus;
import com.example.emergencyassistb4b4.domain.report.repository.ReportRepository;
import com.example.emergencyassistb4b4.domain.report.service.ReportService;
import com.example.emergencyassistb4b4.domain.user.domain.LoginType;
import com.example.emergencyassistb4b4.domain.user.domain.User;
import com.example.emergencyassistb4b4.domain.user.domain.UserRole;
import com.example.emergencyassistb4b4.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportServiceTest {


    @Mock
    ReportRepository reportRepository;
    @Mock ReportResponseRepository reportResponseRepository;
    @Mock UserRepository userRepository;
    @InjectMocks ReportService reportService;

    private Report sampleReport;

    @BeforeEach
    void setUp() {
        //샘플 신고
        User reporter = User.builder()
                .email("user@example.com")
                .password("pass")
                .loginType(LoginType.LOCAL)
                .provider("local")
                .userRole(UserRole.NGO.IND)
                .build();

        sampleReport = Report.builder()
                .reporter(reporter)
                .disasterType(DisasterType.FLOOD)
                .description("desc")
                .imageUrl("img")
                .videoUrl("vid")
                .status(ReportStatus.PENDING)
                .si("A").gu("B")
                .build();

        // id 필드 직접 주입 (리플렉션이나 setter 없이 테스트용으로만)
        ReflectionTestUtils.setField(sampleReport, "id", 1L);

        // ReportRepository.findById(1L) 스텁
        when(reportRepository.findById(1L))
                .thenReturn(Optional.of(sampleReport));

        // 2) 공공기관 유저 셋업: publicId=100L
        User gov = User.builder()
                .email("gov@example.com")
                .password("pass")
                .loginType(LoginType.LOCAL)
                .provider("local")
                .userRole(UserRole.GOV)
                .build();
        when(userRepository.findById(100L))
                .thenReturn(Optional.of(gov));
    }

    @Test
    void testChangeReportStatus() {

        ReportStatusResponseDto dto =
                reportService.changeReportStatus(100L, 1L, ReportStatus.RECEIVED);

        assertThat(dto.reportId()).isEqualTo(1L);
        assertThat(dto.status()).isEqualTo(ReportStatus.RECEIVED);
        assertThat(sampleReport.getStatus()).isEqualTo(ReportStatus.RECEIVED);
        verify(reportRepository).findById(1L);
    }

//    @Test
//    void testBatchStatusChange() {
//        BatchStatusChangeRequest req = new BatchStatusChangeRequest(List.of(1L,2L), ReportStatus.CLOSED);
//
//        // countByIdIn 목 설정
//        when(reportRepository.countByIdIn(List.of(1L,2L))).thenReturn(2L);
//        when(reportRepository.updateStatusBatch(List.of(1L,2L), ReportStatus.CLOSED)).thenReturn(2);
//
//        List<BatchStatusChangeResponseDto> res =
//                reportService.changeStatusBatch(100L, req);
//
//        assertThat(res).hasSize(2)
//                .extracting("reportId","status")
//                .containsExactlyInAnyOrder(
//                        tuple(1L, ReportStatus.CLOSED),
//                        tuple(2L, ReportStatus.CLOSED)
//                );
//        verify(reportRepository).updateStatusBatch(List.of(1L,2L), ReportStatus.CLOSED);
//    }

    @Test
    void testGetMyReports() {
        Pageable pg = PageRequest.of(0, 2);
        LocalDateTime now = LocalDateTime.now();
        List<Report> list = List.of(sampleReport);
        Slice<Report> slice = new SliceImpl<>(list, pg, false);
        when(reportRepository.findByReporter(100L, null, null, null, pg)).thenReturn(slice);

        Slice<ReportDto> dtoSlice =
                reportService.getMyReports(100L,null,null,null,pg);

        assertThat(dtoSlice.getContent()).hasSize(1);
        verify(reportRepository).findByReporter(100L,null,null,null,pg);
    }

}