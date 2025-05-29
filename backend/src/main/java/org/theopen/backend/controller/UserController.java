package org.theopen.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.theopen.backend.dto.TelegramAuthRequest;
import org.theopen.backend.dto.UserDto;
import org.theopen.backend.service.UserService;
import org.theopen.backend.validator.TelegramAuthValidator;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TelegramAuthValidator validator;

    @PostMapping("/auth")
    public ResponseEntity<UserDto> authUser(@RequestBody TelegramAuthRequest authRequest) {
        if (!validator.isValid(authRequest)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDto userDto = userService.registerOrFetch(authRequest);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@RequestHeader("X-Tg-Id") Long tgId) {
        UserDto userDto = userService.findByTelegramId(tgId);
        return ResponseEntity.ok(userDto);
    }
}