package com.healthy.system.enums;

public enum GenderEnum {
    MALE(1),
    FEMALE(2);

    private final int code;

    GenderEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static GenderEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (GenderEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown gender code: " + code);
    }
}

