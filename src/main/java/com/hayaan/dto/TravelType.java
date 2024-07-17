package com.hayaan.dto;

public enum TravelType {
    O("ONE_WAY"),
    T("TWO_WAY");

    private final String type;

    TravelType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
