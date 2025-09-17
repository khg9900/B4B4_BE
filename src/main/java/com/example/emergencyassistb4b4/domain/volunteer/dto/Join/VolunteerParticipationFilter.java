package com.example.emergencyassistb4b4.domain.volunteer.dto.Join;

import com.example.emergencyassistb4b4.domain.volunteer.enums.CheckinStatus;
import com.example.emergencyassistb4b4.domain.volunteer.enums.PostCategory;
import com.example.emergencyassistb4b4.domain.volunteer.enums.PostStatus;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerParticipationFilter {

    private String province;

    private String city;

    private PostStatus postStatus;

    private PostCategory category;

    private CheckinStatus checkinStatus;

    private LocalDate volunteerStartDate;

    private LocalDate volunteerEndDate;

}
