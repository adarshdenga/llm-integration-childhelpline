package com.bdi.agent.model;

import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Data
@Builder
@AllArgsConstructor
public class UserDataResponse {

    @Getter
    private String username;

    @Getter
    private String email;

    @Getter
    private Role role;

    @Getter
    private Collection<? extends GrantedAuthority> authorities;

}
