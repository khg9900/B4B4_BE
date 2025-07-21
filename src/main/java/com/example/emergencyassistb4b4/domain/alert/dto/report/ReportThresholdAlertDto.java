package com.example.emergencyassistb4b4.domain.alert.dto.report;

import com.example.emergencyassistb4b4.domain.alert.domain.report.ReportAlert;
import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReportThresholdAlertDto {

    private String province;
    private String city;
    private String disasterType;
    private Long count;

    private static final String PREFIX = "alert";
    private static final int IDX_CNT = 1;
    private static final int IDX_PROVINCE = 3;
    private static final int IDX_CITY = 4;
    private static final int IDX_TYPE = 5;

    public static ReportThresholdAlertDto fromKey(String notifyKey) {

        // notifyKey ex) alert:10:report:서울:강남구:홍수:20xx-xx-xx
        String[] parts = notifyKey.split(":");

        // 키 검증
        if (!PREFIX.equals(parts[0])) {
            throw new ApiException(ErrorStatus.ALERT_BAD_REQUEST);
        }

        return ReportThresholdAlertDto.builder()
            .province(parts[IDX_PROVINCE])
            .city(parts[IDX_CITY])
            .disasterType(parts[IDX_TYPE])
            .count(Long.parseLong(parts[IDX_CNT]))
            .build();
    }

    public ReportAlert toEntity() {
        return ReportAlert.builder()
            .province(this.province)
            .city(this.city)
            .disasterType(this.disasterType)
            .count(this.count)
            .build();
    }
}
