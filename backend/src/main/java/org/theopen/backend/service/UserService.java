package org.theopen.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.theopen.backend.dto.TelegramAuthRequest;
import org.theopen.backend.dto.UserDto;
import org.theopen.backend.model.User;
import org.theopen.backend.repo.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto registerOrFetch(TelegramAuthRequest request) {
        return userRepository.findByTgId(request.getId())
                .map(UserDto::fromEntity)
                .orElseGet(() -> {
                    User user = new User();
                    user.setTgId(request.getId());
                    user.setName(request.getFirst_name());
                    return UserDto.fromEntity(userRepository.save(user));
                });
    }

    public UserDto findByTelegramId(Long tgId) {
        return userRepository.findByTgId(tgId)
                .map(UserDto::fromEntity)
                .orElseThrow(org.theopen.backend.exception.UserNotFoundException::new);
    }
}

