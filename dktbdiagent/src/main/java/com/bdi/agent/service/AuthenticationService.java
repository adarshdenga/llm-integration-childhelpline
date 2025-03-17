package com.bdi.agent.service;

import com.bdi.agent.authorization.JwtTokenUtils;
import com.bdi.agent.exceptions.EmailAlreadyExistsException;
import com.bdi.agent.exceptions.InvalidCodeException;
import com.bdi.agent.exceptions.UsernameAlreadyExistsException;
import com.bdi.agent.model.AuthenticationRequest;
import com.bdi.agent.model.AuthenticationResponse;
import com.bdi.agent.model.RegisterRequest;
import com.bdi.agent.model.User;
import com.bdi.agent.model.UserDataResponse;
import com.bdi.agent.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Value("${secretCode}")
    private transient String secretCode;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenUtils jwtTokenUtils;
    private final AuthenticationProvider authenticationProvider;

    /**
     * Constructor for authentication service.
     *
     * @param userRepository user repository
     * @param passwordEncoder bcrypt password encoder
     * @param jwtTokenUtils utilities class for operations related to JWT tokens
     * @param authenticationProvider dao authentication provider
     */
    @Autowired
    public AuthenticationService(UserRepository userRepository,
                                 BCryptPasswordEncoder passwordEncoder,
                                 JwtTokenUtils jwtTokenUtils,
                                 AuthenticationProvider authenticationProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtils = jwtTokenUtils;
        this.authenticationProvider = authenticationProvider;
    }

    /**
     * Get a list of all registered users.
     *
     * @return list of all users currently stored in the DB
     */
    public List<UserDataResponse> getAll() {
        // return new ArrayList<User>(userRepository.findAll());
        List<User> users = userRepository.findAll();
        List<UserDataResponse> responses = new ArrayList<>();

        for (User user : users) {
            responses.add(new UserDataResponse(user.getUsername(), user.getEmail(),
                    user.getRole(), user.getAuthorities()));
        }

        return responses;
    }

    /**
     * Retrieves a user's credentials from the registration request
     * and saves them in the database if they are valid (i.e. if the
     * username and email are unique). Then it returns a new JWT token.
     *
     * @param request request containing the user's credentials
     * @throws Exception if the username or email is not unique
     */
    public AuthenticationResponse registerAccount(RegisterRequest request) throws Exception {
        // check if username and/or email already exist and throw exception if they do
        if (this.userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username " + request.getUsername() + " already exists");
        }
        if (this.userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("A user with the email address "
                    + request.getEmail() + " already exists");
        }
        if (!this.verifyCode(request.getCode())) {
            throw new InvalidCodeException("This code is not valid.");
        }

        // else, create a new user and store it in the database
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        userRepository.save(user);
        String token = jwtTokenUtils.generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(request.getRole().toString())
                .build();
    }

    /**
     * Attempts to authenticate based on the request they have sent and generates
     * a new token if authentication is successful.
     *
     * @param request request sent by the user
     * @return an authentication response containing the user's token
     * @throws UsernameNotFoundException if the user's account is not stored in the DB
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) throws UsernameNotFoundException {

        try {
            authenticationProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(request.getUsername() + " does not exist"));

        String token = jwtTokenUtils.generateToken(user);
        List<String> authorities = user.getAuthorities().stream().map(Object::toString).collect(Collectors.toList());

        return AuthenticationResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(authorities.get(0))
                .build();
    }

    /**
     * Verifies code sent by the client.
     *
     * @param code code sent by the client
     * @return true if the code is valid, false otherwise
     */
    private boolean verifyCode(String code) {
        return code.equals(this.secretCode);
    }

}