package com.example.emergencyassistb4b4.domain.userDevice.enums;

public enum DeviceType {
    HANDSET,
    TABLET,
    DESKTOP,
    UNKNOWN;

    public static DeviceType from(String value) {
        return DeviceType.valueOf(value.toUpperCase());
    }
}