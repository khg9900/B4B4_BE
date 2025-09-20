package com.example.emergencyassistb4b4.domain.attendance.socket.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class TrackingMessage<T> {

    private  String type;

    private  T content;
}