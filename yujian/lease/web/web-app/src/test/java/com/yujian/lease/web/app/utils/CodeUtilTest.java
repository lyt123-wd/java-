package com.yujian.lease.web.app.utils;

import com.yujian.lease.common.utils.CodeUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeUtilTest {

    @Test
    void shouldGenerateNumericCodeWithExpectedLength() {
        String code = CodeUtil.getRandomCode(6);
        assertTrue(code.matches("\\d{6}"));
    }
}
