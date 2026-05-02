package com.ielts.helper.enums;

public enum CourseType {
    Listening(1, "Listening"),
    Speaking(2, "Speaking"),
    Reading(3, "Reading"),
    Writing(4, "Writing");

    private final Integer code;
    private final String description;

    CourseType(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static CourseType fromCode(Integer code) {
        for (CourseType type : CourseType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
