package org.theopen.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.theopen.backend.dto.TelegramAuthRequest;
import org.theopen.backend.dto.UserDto;
import org.theopen.backend.model.User;
import org.theopen.backend.service.TelegramValidationService;
import org.theopen.backend.service.UserService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TelegramValidationService validator;


    @PostMapping("/auth")
    public ResponseEntity<UserDto> authUser(@RequestParam String initData) {
        log.info(">> Получено initData: {}", initData);
        Map<String, String> userData = validator.parseAndValidateInitData(initData);
        log.info(">> Распакованные данные пользователя: {}", userData);
        TelegramAuthRequest payload = TelegramAuthRequest.builder()
                .id(Long.valueOf(userData.get("id")))
                .first_name(userData.get("first_name"))
                .build();
        return ResponseEntity.ok(userService.registerOrFetch(payload));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@RequestHeader("X-Tg-Id") Long tgId) {
        User user = userService.findByTelegramId(tgId);
        return ResponseEntity.ok(user);
    }
}