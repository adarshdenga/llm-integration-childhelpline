package com.bdi.agent.authorization;

import com.bdi.agent.model.Role;
import com.bdi.agent.model.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for different scenarios concerning the validation of JWT tokens.
 */

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"mockHttpServletRequest", "mockHttpServletResponse", "mockFilterChain",
        "mockUserDetailsService", "mockJwtTokenUtils"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-test.properties")
public class JwtAuthorizationFilterTest {

    private transient JwtAuthorizationFilter jwtAuthorizationFilter;
    private final String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJpcmVuZSIsImlhdCI6MTY4NTUyMjI5NCwiZXhwIjoxNjg1NjA4Njk0fQ.DpWy82HW9bGCKLTmuR2ONZC8KFMy1Bqxr5_DAmCm8Y4";
    private final User user = new User("username", "password", "email", Role.LEARNER);

    @Autowired
    private transient UserDetailsService mockUserDetailsService;

    @Autowired
    private transient HttpServletRequest httpServletRequest;

    @Autowired
    private transient HttpServletResponse httpServletResponse;

    @Autowired
    private transient FilterChain mockFilterChain;

    @Autowired
    private transient JwtTokenUtils mockJwtTokenUtils;

    /**
     * Sets initial security context holder.
     */
    @BeforeEach
    void setUp() {
        jwtAuthorizationFilter = new JwtAuthorizationFilter(mockJwtTokenUtils, mockUserDetailsService);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    /**
     * Verifies that the chain ends with the JWT Authorization Filter.
     */
    @AfterEach
    void tearDown() throws ServletException, IOException {
        verify(mockFilterChain).doFilter(httpServletRequest, httpServletResponse);
        verifyNoMoreInteractions(mockFilterChain);
    }

    @Test
    void testCorrectToken() throws ServletException, IOException {
        when(httpServletRequest.getHeader(JwtAuthorizationFilter.AUTHORIZATION_HEADER))
                .thenReturn("Bearer " + token);

        // line 52 in JwtAuthorizationFilter
        when(mockJwtTokenUtils.getUsername(token)).thenReturn(user.getUsername());
        // line 53 in JwtAuthorizationFilter
        when(mockUserDetailsService.loadUserByUsername(user.getUsername())).thenReturn(user);
        // line 56 in JwtAuthorizationFilter
        when(mockJwtTokenUtils.isTokenValid(token, user)).thenReturn(true);

        jwtAuthorizationFilter.doFilterInternal(httpServletRequest, httpServletResponse,
                mockFilterChain);

        assertEquals(SecurityContextHolder.getContext().getAuthentication().getName(),
                user.getUsername());
    }

    @Test
    void testInvalidToken() throws ServletException, IOException {
        when(httpServletRequest.getHeader(JwtAuthorizationFilter.AUTHORIZATION_HEADER))
                .thenReturn("Bearer " + token);
        when(mockJwtTokenUtils.isTokenValid(token, user)).thenReturn(false);

        jwtAuthorizationFilter.doFilterInternal(httpServletRequest, httpServletResponse,
                mockFilterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
    }

    private static Stream<Arguments> tokenVerificationExceptionGenerator() {
        return Stream.of(
                Arguments.of(ExpiredJwtException.class),
                Arguments.of(IllegalArgumentException.class),
                Arguments.of(JwtException.class)

        );
    }

    @ParameterizedTest
    @MethodSource("tokenVerificationExceptionGenerator")
    public void tokenVerificationException(Class<? extends Throwable> throwable)
            throws ServletException, IOException {
        when(httpServletRequest.getHeader("Authentication")).thenReturn("Bearer " + token);
        when(mockJwtTokenUtils.isTokenValid(token, user)).thenThrow(throwable);

        jwtAuthorizationFilter.doFilterInternal(httpServletRequest, httpServletResponse, mockFilterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void invalidPrefix() throws ServletException, IOException {
        when(httpServletRequest.getHeader("Authentication")).thenReturn("Bearer1 " + token);
        when(mockJwtTokenUtils.isTokenValid(token, user)).thenReturn(true);
        when(mockJwtTokenUtils.getUsername(token)).thenReturn(user.getUsername());

        jwtAuthorizationFilter.doFilterInternal(httpServletRequest, httpServletResponse, mockFilterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void noPrefix() throws ServletException, IOException {
        when(httpServletRequest.getHeader("Authentication")).thenReturn(token);
        when(mockJwtTokenUtils.isTokenValid(token, user)).thenReturn(true);
        when(mockJwtTokenUtils.getUsername(token)).thenReturn(user.getUsername());

        jwtAuthorizationFilter.doFilterInternal(httpServletRequest, httpServletResponse, mockFilterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }


}