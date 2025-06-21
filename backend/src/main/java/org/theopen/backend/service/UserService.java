package org.theopen.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.theopen.backend.dto.TelegramAuthRequest;
import org.theopen.backend.dto.UserDto;
import org.theopen.backend.model.User;
import org.theopen.backend.repo.UserRepository;
import org.theopen.backend.security.JwtService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${admin.telegram.ids:}")
    private String adminTelegramIds;

    public UserDto registerOrFetch(TelegramAuthRequest request) {
        return userRepository.findById(request.getId())
                .map(user -> {
                    // Проверяем и обновляем роли существующего пользователя
                    updateUserRoles(user);
                    User updatedUser = userRepository.save(user);
                    return generateUserDtoWithToken(updatedUser);
                })
                .orElseGet(() -> {
                    User user = new User();
                    user.setTgId(request.getId());
                    user.setName(request.getFirst_name());

                    // Устанавливаем роль USER для всех пользователей
                    Set<String> roles = new HashSet<>();
                    roles.add("USER");

                    // Проверяем, является ли пользователь администратором
                    if (isAdminTelegramId(request.getId())) {
                        roles.add("ADMIN");
                    }

                    user.setRoles(roles);
                    User savedUser = userRepository.save(user);
                    return generateUserDtoWithToken(savedUser);
                });
    }

    /**
     * Проверяет, является ли Telegram ID администраторским
     *
     * @param tgId Telegram ID пользователя
     * @return true если ID в списке администраторов
     */
    private boolean isAdminTelegramId(Long tgId) {
        if (adminTelegramIds == null || adminTelegramIds.isEmpty()) {
            return false;
        }

        List<Long> adminIds = Arrays.stream(adminTelegramIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();

        return adminIds.contains(tgId);
    }

    /**
     * Обновляет роли пользователя в соответствии с текущими настройками
     *
     * @param user пользователь для обновления
     */
    private void updateUserRoles(User user) {
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }

        // Добавляем базовую роль USER, если её нет
        user.getRoles().add("USER");

        // Проверяем наличие роли ADMIN
        boolean shouldBeAdmin = isAdminTelegramId(user.getTgId());
        boolean isAdmin = user.getRoles().contains("ADMIN");

        // Добавляем или удаляем роль ADMIN в зависимости от настроек
        if (shouldBeAdmin && !isAdmin) {
            user.getRoles().add("ADMIN");
        } else if (!shouldBeAdmin && isAdmin) {
            user.getRoles().remove("ADMIN");
        }
    }

    private UserDto generateUserDtoWithToken(User user) {
        UserDto dto = UserDto.fromEntity(user);
        String token = jwtService.generateToken(user);
        dto.setAccessToken(token);
        return dto;
    }

    public User findByTelegramId(Long tgId) {
        return userRepository.findById(tgId)
                .orElseThrow(org.theopen.backend.exception.UserNotFoundException::new);
    }

    /**
     * Добавляет роль пользователю
     *
     * @param tgId идентификатор пользователя
     * @param role роль для добавления
     * @return обновленный пользователь
     */
    public User addRoleToUser(Long tgId, String role) {
        User user = findByTelegramId(tgId);
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        user.getRoles().add(role);
        return userRepository.save(user);
    }
}
