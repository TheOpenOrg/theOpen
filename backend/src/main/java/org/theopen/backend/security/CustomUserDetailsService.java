package org.theopen.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.theopen.backend.exception.UserNotFoundException;
import org.theopen.backend.model.User;
import org.theopen.backend.repo.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Long tgId = Long.parseLong(username);
            User user = userRepository.findById(tgId)
                    .orElseThrow(UserNotFoundException::new);

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            if (user.getRoles() != null) {
                authorities = user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());
            }

            return new org.springframework.security.core.userdetails.User(
                    user.getTgId().toString(),
                    "", // Пароль не нужен для Telegram-аутентификации
                    true, true, true, true,
                    authorities
            );
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Неверный формат идентификатора пользователя");
        }
    }
}
