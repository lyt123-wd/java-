package com.tandian.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TokenUtilsTest {

    @Test
    void shouldReadRawToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("authorization")).thenReturn("abc123");

        assertEquals("abc123", TokenUtils.resolveToken(request));
    }

    @Test
    void shouldStripBearerPrefix() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("authorization")).thenReturn("Bearer abc123");

        assertEquals("abc123", TokenUtils.resolveToken(request));
    }

    @Test
    void shouldReturnNullForBlankToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("authorization")).thenReturn("   ");

        assertNull(TokenUtils.resolveToken(request));
    }
}
