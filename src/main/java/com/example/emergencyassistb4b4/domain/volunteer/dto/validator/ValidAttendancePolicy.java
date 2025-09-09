package com.example.emergencyassistb4b4.domain.volunteer.dto.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.TYPE }) // DTO 클래스 단위에서 검사
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AttendancePolicyConstraintValidator.class)
@Documented
public @interface ValidAttendancePolicy {

    String message() default "출석 정책 값이 유효하지 않습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

