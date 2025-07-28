<<<<<<<< HEAD:src/main/java/com/example/emergencyassistb4b4/domain/attendance/rabbitmq/dto/MessageWrapper.java
package com.example.emergencyassistb4b4.domain.attendance.rabbitmq.dto;
========
package com.example.emergencyassistb4b4.domain.attendance.dto;
>>>>>>>> 91a6ba41dbd82173278648d8e0dd59d73ebbb3e5:src/main/java/com/example/emergencyassistb4b4/domain/attendance/dto/MessageWrapper.java

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageWrapper {
    private SessionState sessionState;
    private TrackingSessionDto payload;
}
