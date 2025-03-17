package com.bdi.agent.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.bdi.agent.authorization.JwtTokenUtils;
import com.bdi.agent.exceptions.EmailAlreadyExistsException;
import com.bdi.agent.exceptions.InvalidCodeException;
import com.bdi.agent.exceptions.UsernameAlreadyExistsException;
import com.bdi.agent.model.AuthenticationRequest;
import com.bdi.agent.model.AuthenticationResponse;
import com.bdi.agent.model.RegisterRequest;
import com.bdi.agent.model.Role;
import com.bdi.agent.model.User;
import com.bdi.agent.repository.UserRepository;
import com.bdi.agent.service.AuthenticationService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles({"mockJwtTokenUtils", "mockUserRepository", "mockPasswordEncoder", "mockAuthenticationProvider"})
@TestPropertySource(locations="classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AuthenticationServiceTest {

    @Autowired
    private transient UserRepository mockUserRepository;

    @Autowired
    private transient PasswordEncoder mockPasswordEncoder;

    @Autowired
    private transient JwtTokenUtils mockJwtTokenUtils;

    @Autowired
    private transient AuthenticationProvider mockAuthenticationProvider;

    @Autowired
    private transient AuthenticationService authenticationService;

    @Value("${secretCode}")
    private transient String secretCode;

    @Test
    void registerAccountSuccessful() throws Exception {
        User user = User.builder()
                .username("username")
                .password("ENCODED-PASSWORD")
                .email("email")
                .role(Role.LEARNER)
                .build();

        RegisterRequest request = new RegisterRequest("username",
                "password", "email", Role.LEARNER, secretCode);

        // user has not been registered yet
        when(mockUserRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(mockUserRepository.existsByEmail(request.getEmail())).thenReturn(false);

        // returns encoded password
        when(mockPasswordEncoder.encode(request.getPassword())).thenReturn(user.getPassword());

        // returns JWT token
        when(mockJwtTokenUtils.generateToken(user)).thenReturn("exampleToken");

        // authentication response that contains the JWT token
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("exampleToken").username(request.getUsername()).role(request.getRole().toString())
                .build();


        AuthenticationResponse actualResponse = authenticationService.registerAccount(request);
        // verify that user is saved
        verify(mockUserRepository).save(user);

        assertEquals(response, actualResponse);
    }

    @Test
    void registerAccount_UsernameAlreadyExists() {
        when(mockUserRepository.existsByUsername("user1")).thenReturn(true);
        RegisterRequest request = new RegisterRequest("user1",
                "password1", "email1", Role.LEARNER, secretCode);

        // assert that exception is thrown
        assertThrows(UsernameAlreadyExistsException.class, () -> {
            authenticationService.registerAccount(request);
        });
    }

    @Test
    void registerAccount_EmailAlreadyExists() {
        when(mockUserRepository.existsByEmail("email1")).thenReturn(true);
        RegisterRequest request = new RegisterRequest("user1",
                "password1", "email1", Role.LEARNER, secretCode);

        // assert that exception is thrown
        assertThrows(EmailAlreadyExistsException.class, () -> {
            authenticationService.registerAccount(request);
        });
    }

    @Test
    void authenticateSuccessful() {
        String username = "username";
        String password = "password";

        User user = new User(username, password, "email", Role.LEARNER);

        Authentication authentication = mock(Authentication.class);
        authentication.setAuthenticated(true);
        when(mockAuthenticationProvider.authenticate(new UsernamePasswordAuthenticationToken(username, password)))
                .thenReturn(authentication);

        // get user from the database
        when(mockUserRepository.findByUsername(username)).thenReturn(Optional.of(user));
        // returns JWT token
        when(mockJwtTokenUtils.generateToken(user)).thenReturn("exampleToken");

        // authentication response that contains the JWT token
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("exampleToken").username(username).role("LEARNER")
                .build();

        AuthenticationResponse actualResponse = authenticationService
                .authenticate(new AuthenticationRequest(username, password));

        assertEquals(response, actualResponse);
    }

    @Test
    void authenticate_UsernameNotFound() {
        Authentication authentication = mock(Authentication.class);
        authentication.setAuthenticated(true);
        when(mockAuthenticationProvider
                .authenticate(new UsernamePasswordAuthenticationToken("user", "password")))
                .thenReturn(authentication);

        when(mockUserRepository.findByUsername("user")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            authenticationService.authenticate(new AuthenticationRequest("user", "password"));
        });

    }

    @Test
    void authenticate_InvalidCredentials() {
        when(mockAuthenticationProvider
                .authenticate(any(Authentication.class)))
                .thenThrow(BadCredentialsException.class);

        assertThrows(Exception.class, () -> {
            authenticationService.authenticate(new AuthenticationRequest("user", "password"));
        });
    }

    @Test
    void register_InvalidCode() {
        when(mockUserRepository.existsByUsername("user1")).thenReturn(false);
        when(mockUserRepository.existsByEmail("email1")).thenReturn(false);

        RegisterRequest request = new RegisterRequest("user1",
                "password1", "email1", Role.LEARNER, secretCode + "A");

        // assert that exception is thrown
        assertThrows(InvalidCodeException.class, () -> {
            authenticationService.registerAccount(request);
        });
    }
}