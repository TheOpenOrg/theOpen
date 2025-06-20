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
    private final TokenService tokenService;

    public UserDto registerOrFetch(TelegramAuthRequest request) {
        return userRepository.findById(request.getId())
                .map(UserDto::fromEntity)
                .orElseGet(() -> {
                    User user = new User();
                    user.setTgId(request.getId());
                    user.setName(request.getFirst_name());
                    return UserDto.fromEntity(userRepository.save(user));
                });
    }

    public User findByTelegramId(Long tgId) {
        return userRepository.findById(tgId)
                .orElseThrow(org.theopen.backend.exception.UserNotFoundException::new);
    }

    /**
     * Проверяет права доступа пользователя по Telegram ID и токену авторизации
     *
     * @param tgId идентификатор пользователя
     * @param authToken токен авторизации (может быть null)
     * @return true если доступ разрешен
     */
    public boolean validateUserAccess(Long tgId, String authToken) {
        // Проверяем, что пользователь существует
        if (!userRepository.existsById(tgId)) {
            return false;
        }

        // Если токен не передан, используем упрощенную аутентификацию только по TG ID
        // Это можно изменить на более строгую проверку, требующую токен всегда
        if (authToken == null || authToken.isBlank()) {
            return true; // В продакшене здесь лучш�� вернуть false, требуя всегда токен
        }

        // Проверяем токен и привязку к ��ользователю
        return tokenService.validateToken(authToken, tgId);
    }

    /**
     * Генерирует токен доступа для пользователя
     *
     * @param tgId идентификатор пользователя
     * @return токен доступа
     */
    public String generateAccessToken(Long tgId) {
        if (!userRepository.existsById(tgId)) {
            throw new org.theopen.backend.exception.UserNotFoundException();
        }
        return tokenService.generateAccessToken(tgId);
    }
}
