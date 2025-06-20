package org.theopen.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public ResponseEntity<UserDto> authUser(@RequestParam String initData) throws JsonProcessingException {
        log.info(">> Получено initData: {}", initData);
        Map<String, String> userData = validator.parseAndValidateInitData(initData);
        log.info(">> Распакованные данные пользователя: {}", userData);
        String userJson = userData.get("user");
        TelegramAuthRequest authRequest = new ObjectMapper().readValue(userJson, TelegramAuthRequest.class);
        return ResponseEntity.ok(userService.registerOrFetch(authRequest));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@RequestHeader("X-Tg-Id") Long tgId) {
        User user = userService.findByTelegramId(tgId);
        return ResponseEntity.ok(user);
    }
}