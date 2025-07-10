package com.example.emergencyassistb4b4.alert.enums;

public enum AlertType {
    DISASTER,
    VOLUNTEER;

    public static AlertType from(String value) {
        return AlertType.valueOf(value.toUpperCase());
    }
}
