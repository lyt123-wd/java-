package com.yujian.lease.web.app.utils;

import com.yujian.lease.common.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TokenUtilsTest {

    @Test
    void shouldReadAccessTokenHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("access-token")).thenReturn("abc");

        assertEquals("abc", TokenUtils.resolveToken(request));
    }

    @Test
    void shouldReadBearerAuthorization() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("authorization")).thenReturn("Bearer abc");

        assertEquals("abc", TokenUtils.resolveToken(request));
    }

    @Test
    void shouldReturnNullWhenMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        assertNull(TokenUtils.resolveToken(request));
    }
}
