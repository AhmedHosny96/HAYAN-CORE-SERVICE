package com.hayaan.dto;


public enum PassengerType {
    ADT("ADULT"),
    CHD("CHILD"),
    INF("INFANT");

    private final String code;

    PassengerType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
