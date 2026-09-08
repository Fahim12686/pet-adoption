package com.seu.petadoptionservice.config;

import com.seu.petadoptionservice.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService customUserDetailsService, PasswordEncoder passwordEncoder) {
        // 1. Pass the UserDetailsService directly into the constructor
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailsService);

        // 2. Continue setting the password encoder normally
        authProvider.setPasswordEncoder(passwordEncoder);

        // 3. Remove the authProvider.setUserDetailsService(...) line entirely
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        try {
            http
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/admin/**").hasRole("ADMIN")
                            // Specific routes must come BEFORE the generic {id} wildcard
                            .requestMatchers("/pets/new", "/pets/save", "/pets/edit/**", "/pets/delete/**").hasAnyRole("ADMIN", "STAFF")
                            // Added regex \\d+ so this only matches numeric IDs, preventing it from shadowing "new" or "save"
                            .requestMatchers("/", "/pets", "/pets/{id:\\d+}", "/store", "/signup", "/signin", "/css/**", "/js/**", "/images/**").permitAll()
                            .anyRequest().authenticated()
                    )
                    .formLogin(form -> form
                            .loginPage("/signin")
                            .defaultSuccessUrl("/dashboard", true)
                            .failureUrl("/signin?error=true")
                            .permitAll()
                    )
                    .logout(logout -> logout
                            .logoutSuccessUrl("/")
                            .permitAll()
                    );

            return http.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}