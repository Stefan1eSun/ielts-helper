package com.ielts.helper.enums;

public enum CourseStatus {
    PendingPayment(1, "pending_payment"),
    Confirmed(2, "confirmed"),
    InProgress(3, "in_progress"),
    Completed(4, "completed"),
    Cancelled(5, "cancelled");

    private final Integer code;
    private final String value;

    CourseStatus(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public Integer getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static CourseStatus fromCode(Integer code) {
        for (CourseStatus status : CourseStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    public static CourseStatus fromValue(String value) {
        for (CourseStatus status : CourseStatus.values()) {
            if (status.getValue().equals(value)) {
                return status;
            }
        }
        return null;
    }
}
