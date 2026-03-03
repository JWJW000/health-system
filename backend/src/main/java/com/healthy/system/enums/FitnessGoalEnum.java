package com.healthy.system.enums;

public enum FitnessGoalEnum {
    BULK(1, "增肌"),
    CUT(2, "减脂"),
    SHAPE(3, "塑形"),
    FUNCTIONAL(4, "功能性训练");

    private final int code;
    private final String label;

    FitnessGoalEnum(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static FitnessGoalEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (FitnessGoalEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown fitness goal code: " + code);
    }
}

