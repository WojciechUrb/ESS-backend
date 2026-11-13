package com.urbaniak.evacuation_support_system.security;

import com.urbaniak.evacuation_support_system.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService  implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UUID uuid;

        try {
            uuid = UUID.fromString(username);
        } catch (IllegalArgumentException ex) {
            throw new UsernameNotFoundException("Nieprawidłowy identyfikator użytkownika: " + username);
        }

        return userRepository.findByUuid(uuid).map(UserPrincipal::new).orElseThrow(() ->
                new UsernameNotFoundException("Nie znaleziono użytkownika o uuid: " + username));

    }
}
