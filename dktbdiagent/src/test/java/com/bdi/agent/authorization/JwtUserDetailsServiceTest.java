package com.bdi.agent.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.bdi.agent.model.Role;
import com.bdi.agent.model.User;
import com.bdi.agent.repository.UserRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles({"mockUserRepository"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(locations="classpath:application-test.properties")
public class JwtUserDetailsServiceTest {

    @Autowired
    private transient UserRepository mockUserRepository;

    @Autowired
    private transient JwtUserDetailsService jwtUserDetailsService;

    @Test
    void testLoadUserByUsername_Successful() {
        User user = new User("username", "password", "email@gmail.com", Role.LEARNER);
        when(mockUserRepository.findByUsername("username")).thenReturn(Optional.of(user));
        assertEquals(user, jwtUserDetailsService.loadUserByUsername(user.getUsername()));
    }

    @Test
    void testLoadUserByUsername_UserDoesNotExist() {
        when(mockUserRepository.findByUsername(any())).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> {
            jwtUserDetailsService.loadUserByUsername("someUser");
        });
    }
}