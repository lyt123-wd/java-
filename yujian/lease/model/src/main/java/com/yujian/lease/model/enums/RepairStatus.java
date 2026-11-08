package com.yujian.lease.model.enums;

/**
 * 报修工单状态常量。
 */
public final class RepairStatus {

    public static final int PENDING = 0;
    public static final int PROCESSING = 1;
    public static final int COMPLETED = 2;
    public static final int CANCELED = 3;

    private RepairStatus() {
    }

    public static boolean canHandle(int status) {
        return status == PENDING || status == PROCESSING;
    }

    public static boolean canCancel(int status) {
        return status == PENDING || status == PROCESSING;
    }
}
