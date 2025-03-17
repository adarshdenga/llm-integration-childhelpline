package com.bdi.agent.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.bdi.agent.model.AuthenticationRequest;
import com.bdi.agent.model.AuthenticationResponse;
import com.bdi.agent.model.RegisterRequest;
import com.bdi.agent.service.AuthenticationService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final transient AuthenticationService authenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Endpoint for authenticating a user.
     *
     * @param request request containing a user's credentials
     * @return 200 OK and a token
     * @throws RuntimeException if the user's account is not stored in the DB
     */
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest request)
            throws RuntimeException {
        try {
            AuthenticationResponse authResponse = authenticationService.authenticate(request);
            return ResponseEntity.ok(authResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    /**
     * Endpoint for registering a new account.
     *
     * @param request request containing the user's credentials
     * @return 200 OK and a user entity
     * @throws ResponseStatusException if the username or email is already in use
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request)
            throws RuntimeException {
        try {
            AuthenticationResponse user = authenticationService.registerAccount(request);
            return ResponseEntity.ok(user);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

}
