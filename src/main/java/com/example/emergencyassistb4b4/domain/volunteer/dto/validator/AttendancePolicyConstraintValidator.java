package com.example.emergencyassistb4b4.domain.volunteer.dto.validator;

import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.CreatePostRequest;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.UpdatePostRequest;
import com.example.emergencyassistb4b4.domain.volunteer.dto.Post.common.PostAttendancePolicyDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class AttendancePolicyConstraintValidator
        implements ConstraintValidator<ValidAttendancePolicy, Object> {

    @Override
    public boolean isValid(Object dto, ConstraintValidatorContext context) {
        PostAttendancePolicyDto policy = extractPolicy(dto);
        if (policy == null) return true;

        LocalDateTime now = LocalDateTime.now();
        boolean valid = true;

        if (policy.getCheckinStart() != null && policy.getCheckinEnd() != null) {

            if (policy.getCheckinStart().isAfter(policy.getCheckinEnd())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("출석 시작 시간이 종료 시간보다 늦습니다.")
                        .addPropertyNode("attendancePolicy.checkinStart")
                        .addConstraintViolation();
                valid = false;
            }

            if (policy.getCheckinEnd().isBefore(now)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("출석 종료 시간이 이미 지났습니다.")
                        .addPropertyNode("attendancePolicy.checkinEnd")
                        .addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }

    private PostAttendancePolicyDto extractPolicy(Object dto) {
        if (dto instanceof CreatePostRequest create) {
            return create.getAttendancePolicy();
        } else if (dto instanceof UpdatePostRequest update) {
            return update.getAttendancePolicy();
        } else {
            return null;
        }
    }
}
