package com.bdi.agent.authorization;

import com.bdi.agent.model.User;
import com.bdi.agent.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    private final transient UserRepository userRepository;

    @Autowired
    public JwtUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User does not exist"));
        return new User(user.getUsername(), user.getPassword(), user.getEmail(), user.getRole());
    }

    /**
     * Updates the password of the given user.
     *
     * @param username name of the user
     * @param newPassword new password
     * @throws UsernameNotFoundException if there is no user with the given username
     */
    public void updatePassword(String username, String newPassword) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User does not exist"));

        userRepository.delete(user);
        user.setPassword(newPassword);
        userRepository.save(user);
    }

}
