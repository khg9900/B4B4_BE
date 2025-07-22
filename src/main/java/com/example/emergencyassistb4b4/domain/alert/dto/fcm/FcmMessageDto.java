package com.example.emergencyassistb4b4.domain.alert.dto.fcm;

import com.example.emergencyassistb4b4.domain.alert.dto.report.ReportThresholdAlertDto;
import com.example.emergencyassistb4b4.domain.alert.dto.report.ReportImmediateAlertDto;
import com.example.emergencyassistb4b4.domain.alert.dto.volunteer.VolunteerUpdateAlertDto;
import java.time.format.DateTimeFormatter;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FcmMessageDto {

    private String title;
    private String body;

    public static FcmMessageDto fromReportThresholdAlert(ReportThresholdAlertDto alert) {

        String title = String.format(
            "[재난 알림] %s %s %s 발생 알림",
            alert.getProvince(), alert.getCity(), alert.getDisasterType()
        );
        String body = String.format(
            "%s %s에서 %s 신고가 %s건 이상 접수되었습니다.",
            alert.getProvince(), alert.getCity(), alert.getDisasterType(), alert.getCount()
        );

        return FcmMessageDto.builder()
            .title(title)
            .body(body)
            .build();
    }

    public static FcmMessageDto fromReportImmediateAlert(ReportImmediateAlertDto alert) {

        String title = String.format(
            "[재난 신고 접수] %s %s %s 신고",
            alert.getProvince(), alert.getCity(), alert.getDisasterType()
        );

        String body = String.format(
            """
                재난 유형 : %s
                신고 내용 : %s
                발생 장소 : %s %s
                발생 시간 : %s
                """,
            alert.getDisasterType(),
            alert.getDescription(),
            alert.getProvince(), alert.getCity(),
            alert.getReportedAt().format(DateTimeFormatter.ofPattern("MM월 dd일 HH:mm"))
        );

        return FcmMessageDto.builder()
            .title(title)
            .body(body)
            .build();
    }

    public static FcmMessageDto fromVolunteerUpdateAlert(VolunteerUpdateAlertDto alert) {

        String title = String.format(
            "[봉사 알림] %s 변경 공지", alert.getTitle()
        );

        String body = String.format(
            """
                게시글명 : %s
                변경 장소 : %s
                변경 시간 : %s
                """,
            alert.getTitle(),
            alert.getPlaceName(),
            alert.getCheckinStart().format(DateTimeFormatter.ofPattern("MM월 dd일 HH:mm"))
        );

        return FcmMessageDto.builder()
            .title(title)
            .body(body)
            .build();
    }
}
