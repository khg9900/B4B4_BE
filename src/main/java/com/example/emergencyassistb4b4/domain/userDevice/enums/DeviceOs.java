package com.example.emergencyassistb4b4.domain.userDevice.enums;

public enum DeviceOs {
    ANDROID,
    IOS;

    public static DeviceOs from(String value) {
        return DeviceOs.valueOf(value.toUpperCase());
    }
}
