package com.example.emergencyassistb4b4.report.enums;

import lombok.Getter;

@Getter
public enum ReportStatus {
    PENDING("대기"),   //대기
    RECEIVED("확인"),  //확인
    CLOSED("종료");     //종료

    private final String description;

    ReportStatus(String description){
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

    //상태 전이 유효 검사
    public boolean canTransitionTo(ReportStatus next) {
        switch (this) {
            case PENDING:  return next == RECEIVED;
            case RECEIVED: return next == CLOSED;
            default:       return false;
        }
    }
    //다음 상태 변경
    public ReportStatus transitionTo(ReportStatus next) {
        if (!canTransitionTo(next)) {
            throw new IllegalStateException(
                    String.format("상태는 %s → %s 로 변경할 수 없습니다.", this, next)
            );
        }
        return next;
    }
}
