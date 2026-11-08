package com.yujian.lease.common.utils;

import java.security.SecureRandom;

public final class CodeUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private CodeUtil() {
    }

    public static String getRandomCode(Integer length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(RANDOM.nextInt(10));
        }
        return builder.toString();
    }
}
