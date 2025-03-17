package com.bdi.agent.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.bdi.agent.authorization.JwtTokenUtils;
import com.bdi.agent.model.AuthenticationRequest;
import com.bdi.agent.model.RegisterRequest;
import com.bdi.agent.model.Role;
import com.bdi.agent.model.User;
import com.bdi.agent.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"mockJwtTokenUtils", "mockUserRepository", "mockPasswordEncoder", "mockAuthenticationProvider"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-test.properties")
@AutoConfigureMockMvc
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient UserRepository mockUserRepository;

    @Autowired
    private transient PasswordEncoder mockPasswordEncoder;

    @Autowired
    private transient JwtTokenUtils mockJwtTokenUtils;

    @Autowired
    private transient AuthenticationProvider mockAuthenticationProvider;

    @Value("${secretCode}")
    private transient String secretCode;

    @Test
    void registerSuccessful() throws Exception {
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

        ObjectMapper objectMapper = new ObjectMapper();
        String modelSerialized = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(modelSerialized));

        result.andExpect(status().isOk());
    }

    @Test
    void registerAccount_UsernameAlreadyExists() throws Exception {
        when(mockUserRepository.existsByUsername("user1")).thenReturn(true);
        RegisterRequest request = new RegisterRequest("user1",
                "password1", "email1", Role.LEARNER, secretCode);

        ObjectMapper objectMapper = new ObjectMapper();
        String modelSerialized = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(modelSerialized));

        result.andExpect(status().is(409));
    }

    @Test
    void registerAccount_EmailAlreadyExists() throws Exception {
        when(mockUserRepository.existsByEmail("email1")).thenReturn(true);
        RegisterRequest request = new RegisterRequest("user1",
                "password1", "email1", Role.LEARNER, secretCode);

        ObjectMapper objectMapper = new ObjectMapper();
        String modelSerialized = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(modelSerialized));

        result.andExpect(status().is(409));
    }

    @Test
    void registerAccount_InvalidCode() throws Exception {
        when(mockUserRepository.existsByEmail("email1")).thenReturn(false);
        when(mockUserRepository.existsByEmail("email1")).thenReturn(false);

        RegisterRequest request = new RegisterRequest("user1",
                "password1", "email1", Role.LEARNER, secretCode + "A");

        ObjectMapper objectMapper = new ObjectMapper();
        String modelSerialized = objectMapper.writeValueAsString(request);

        ResultActions result = mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(modelSerialized));

        result.andExpect(status().is(409));
    }


    @Test
    void authenticateSuccessful() throws Exception {
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

        ObjectMapper objectMapper = new ObjectMapper();
        String modelSerialized = objectMapper.writeValueAsString(new AuthenticationRequest(username, password));

        ResultActions result = mockMvc.perform(post("/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(modelSerialized));

        result.andExpect(status().isOk());
    }

    @Test
    void authenticate_UsernameNotFound() throws Exception {
        Authentication authentication = mock(Authentication.class);
        authentication.setAuthenticated(true);
        when(mockAuthenticationProvider
                .authenticate(new UsernamePasswordAuthenticationToken("user", "password")))
                .thenReturn(authentication);

        when(mockUserRepository.findByUsername("user")).thenReturn(Optional.empty());

        ObjectMapper objectMapper = new ObjectMapper();
        String modelSerialized = objectMapper
                .writeValueAsString(new AuthenticationRequest("user", "password"));

        ResultActions result = mockMvc.perform(post("/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(modelSerialized));

        result.andExpect(status().is(409));
    }

    @Test
    void authenticate_InvalidCredentials() throws Exception {
        when(mockAuthenticationProvider
                .authenticate(any(Authentication.class)))
                .thenThrow(BadCredentialsException.class);

        ObjectMapper objectMapper = new ObjectMapper();
        String modelSerialized = objectMapper
                .writeValueAsString(new AuthenticationRequest("user", "password"));

        ResultActions result = mockMvc.perform(post("/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(modelSerialized));

        result.andExpect(status().is(409));
    }

}

