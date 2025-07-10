package com.example.emergencyassistb4b4.volunteer.domain;

import com.example.emergencyassistb4b4.global.exception.ApiException;
import com.example.emergencyassistb4b4.global.status.ErrorStatus;
import com.example.emergencyassistb4b4.user.domain.User;
import com.example.emergencyassistb4b4.volunteer.enums.CheckinStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VolunteerParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private VolunteerTeam volunteerTeam;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CheckinStatus checkinStatus;

    public void updateStatus(CheckinStatus newStatus) {
        if (this.checkinStatus == CheckinStatus.BLACKLISTED) {
            throw new ApiException(ErrorStatus.VOLUNTEER_BAD_REQUEST);
            //IllegalStateException("블랙리스트는 상태 변경이 불가능합니다.");
        }
        this.checkinStatus = newStatus;
    }

}
