package com.tandian.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegexUtilsTest {

    @Test
    void shouldValidatePhoneNumber() {
        assertFalse(RegexUtils.isPhoneInvalid("13812345678"));
        assertTrue(RegexUtils.isPhoneInvalid("12345"));
        assertTrue(RegexUtils.isPhoneInvalid(""));
        assertTrue(RegexUtils.isPhoneInvalid(null));
    }

    @Test
    void shouldValidateEmail() {
        assertFalse(RegexUtils.isEmailInvalid("dev@example.com"));
        assertTrue(RegexUtils.isEmailInvalid("not-an-email"));
    }

    @Test
    void shouldValidateCode() {
        assertFalse(RegexUtils.isCodeInvalid("123456"));
        assertFalse(RegexUtils.isCodeInvalid("a1B2c3"));
        assertTrue(RegexUtils.isCodeInvalid("123"));
    }
}
