package com.bdi.agent.authorization;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.TestPropertySource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest
@TestPropertySource(locations="classpath:application-test.properties")
public class JwtAuthEndpointTest {

    private final transient JwtAuthEndpoint jwtAuthEndpoint = new JwtAuthEndpoint();
    @MockBean
    private transient HttpServletRequest mockRequest;
    @MockBean
    private transient HttpServletResponse mockResponse;
    @MockBean
    private transient AuthenticationException authenticationException;

    @Test
    void commenceTest() throws ServletException, IOException {
        jwtAuthEndpoint.commence(mockRequest, mockResponse, authenticationException);

        // When a user is not authorized to access a certain resource, their request is
        // directly sent to the authorization endpoint, so there are no interactions with
        // the HTTP request (e.g. no information is retrieved from it)
        verifyNoInteractions(mockRequest);

        // Verifies that commence() adds the authorization header to its response
        verify(mockResponse).addHeader(JwtAuthorizationFilter.WWW_AUTHENTICATE_HEADER,
                JwtAuthorizationFilter.TOKEN_PREFIX);

        // Verifies that an HTTP 401 error response is sent
        verify(mockResponse).sendError(HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase());
    }
}