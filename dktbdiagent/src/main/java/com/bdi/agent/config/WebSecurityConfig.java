package com.bdi.agent.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.bdi.agent.authorization.JwtAuthEndpoint;
import com.bdi.agent.authorization.JwtAuthorizationFilter;
import com.bdi.agent.authorization.JwtUserDetailsService;

/**
 * Some parts of the code related to Authentication are inspired by the tutorial
 * published by the English content creator Nelson Djalo, which can be found in this
 * Github repository: https://github.com/ali-bouali/spring-boot-3-jwt-security
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final transient JwtAuthorizationFilter jwtAuthorizationFilter;
    private final transient JwtAuthEndpoint jwtAuthEndpoint;
    private final transient JwtUserDetailsService jwtUserDetailsService;

    /**
     * Web security configuration.
     *
     * @param jwtAuthorizationFilter filter that checks whether the user is authorized
     * @param jwtAuthEndpoint entry point for authentication errors
     * @param jwtUserDetailsService service for retrieving users
     */
    @Autowired
    public WebSecurityConfig(JwtAuthorizationFilter jwtAuthorizationFilter,
                             JwtAuthEndpoint jwtAuthEndpoint, JwtUserDetailsService jwtUserDetailsService) {
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
        this.jwtAuthEndpoint = jwtAuthEndpoint;
        this.jwtUserDetailsService = jwtUserDetailsService;
    }

    /**
     * Returns a bean for a password encoder.
     *
     * @return password encoder
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Returns a bean for a database authentication provider.
     *
     * @return database authentication provider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(jwtUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Returns a bean for an authentication manager.
     *
     * @return authentication manager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder());
    }

    /**
     * Security configuration for HTTP requests.
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors().and()
                .csrf().disable()
                .authorizeRequests()
                //.antMatchers("**").permitAll()
                .antMatchers("/auth/**").permitAll()
                .antMatchers("/session/**").permitAll()
                .antMatchers("/beliefs/**").permitAll()
                .antMatchers("/topic/**").permitAll()
                .antMatchers("/app/**").permitAll()
                .antMatchers("/agent/**").permitAll()
                .antMatchers("/create/**").permitAll()
                .antMatchers("/trainer/**").hasAnyAuthority("TRAINER", "ADMIN")
                .antMatchers("/user/**").hasAnyAuthority("LEARNER", "TRAINER", "ADMIN")

                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(jwtAuthEndpoint)
                .and()
                //.authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
    }

}
