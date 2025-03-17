package com.bdi.agent.authorization;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * When a user is not authorized to access a certain resource, their request is sent
 * to this endpoint and an error message is returned.
 */
@Component
public class JwtAuthEndpoint implements AuthenticationEntryPoint {

    /**
     * Commences an authentication scheme.
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {

        // Return an unauthorized response code
        response.addHeader(JwtAuthorizationFilter.WWW_AUTHENTICATE_HEADER, JwtAuthorizationFilter.TOKEN_PREFIX);
        response.sendError(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.getReasonPhrase());
    }
}
