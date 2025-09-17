package com.example.emergencyassistb4b4.domain.alert.dto.fcm;

import com.example.emergencyassistb4b4.domain.alert.dto.report.ReportThresholdAlertDto;
import com.example.emergencyassistb4b4.domain.alert.dto.report.ReportImmediateAlertDto;
import com.example.emergencyassistb4b4.domain.alert.dto.volunteer.VolunteerPostAlertDto;
import java.time.format.DateTimeFormatter;
import lombok.Builder;
import lombok.Getter;

import static org.apache.kafka.common.utils.Sanitizer.sanitize;

@Getter
@Builder
public class FcmMessageDto {

    private String title;
    private String body;

    private static String joinSpace(String province, String city) {

        String p = sanitize(province);
        String c = sanitize(city);

        return c.isEmpty() ? p : p + " " + c;
    }

    private static String sanitize(String s) {

        if (s == null) return "";

        String t = s.trim();

        return t.isEmpty() || "null".equalsIgnoreCase(t) ? "" : t;
    }

    public static FcmMessageDto fromReportThresholdAlert(ReportThresholdAlertDto alert) {

        String place = joinSpace(alert.getProvince(), alert.getCity());

        String title = String.format(
            "[재난 알림] %s %s 발생 알림",
            place, alert.getDisasterType()
        );

        String body = String.format(
            "%s에서 %s 신고가 %d건 이상 접수되었습니다.",
            place, alert.getDisasterType(), alert.getCount()
        );

        return FcmMessageDto.builder()
            .title(title)
            .body(body)
            .build();
    }

    public static FcmMessageDto fromReportImmediateAlert(ReportImmediateAlertDto alert) {

        String place = joinSpace(alert.getProvince(), alert.getCity());

        String title = String.format(
            "[재난 신고 접수] %s %s 신고",
            place, alert.getDisasterType()
        );

        String body = String.format(
            """
            재난 유형 : %s
            신고 내용 : %s
            발생 장소 : %s
            발생 시간 : %s
            """,
            alert.getDisasterType(),
            alert.getDescription(),
            place,
            alert.getReportedAt().format(DateTimeFormatter.ofPattern("MM월 dd일 HH:mm"))
        );

        return FcmMessageDto.builder()
            .title(title)
            .body(body)
            .build();
    }

    public static FcmMessageDto fromVolunteerUpdateAlert(VolunteerPostAlertDto alert) {

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
            alert.getVolunteerDate().format(DateTimeFormatter.ofPattern("MM월 dd일 HH:mm"))
        );

        return FcmMessageDto.builder()
            .title(title)
            .body(body)
            .build();
    }

    public static FcmMessageDto fromVolunteerCancelAlert(VolunteerPostAlertDto alert) {

        String title = String.format(
                "[봉사 알림] %s 취소 공지", alert.getTitle()
        );

        String body = String.format(
                """
                    게시글명 : %s
                    취소 장소 : %s
                    취소 시간 : %s
                    """,
                alert.getTitle(),
                alert.getPlaceName(),
                alert.getVolunteerDate().format(DateTimeFormatter.ofPattern("MM월 dd일 HH:mm"))
        );

        return FcmMessageDto.builder()
                .title(title)
                .body(body)
                .build();
    }
}
