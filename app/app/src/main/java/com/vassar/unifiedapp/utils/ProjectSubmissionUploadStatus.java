package com.vassar.unifiedapp.utils;
import android.provider.ContactsContract;

import java.util.HashMap;
import java.util.Map;

public enum ProjectSubmissionUploadStatus {

    UNSYNCED(0),
    SYNCED(1),
    SYNCED_WITH_MEDIA(2),
    VALIDATION_ERROR(-1),
    SERVER_ERROR(-2),
    FAILED(-3),
    DELETED(-4),
    APP_VERSION_MISMATCH_ERROR(-5);

    private static final Map<Integer, ProjectSubmissionUploadStatus> valueToStateNameMap = new HashMap<>();
    private final Integer value;

    static {
        for (ProjectSubmissionUploadStatus myEnum : values()) {
            valueToStateNameMap.put(myEnum.getValue(), myEnum);
        }
    }

    private ProjectSubmissionUploadStatus(final Integer newValue) {
        value = newValue;
    }

    public Integer getValue() {
        return value;
    }

    public static ProjectSubmissionUploadStatus getStateByValue(Integer value) {
        return valueToStateNameMap.get(value);
    }
}
