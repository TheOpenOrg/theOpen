package org.theopen.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
        UserDto userDto = userService.registerOrFetch(authRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + userDto.getAccessToken());

        return ResponseEntity.ok()
                .headers(headers)
                .body(userDto);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        Long tgId = Long.parseLong(userDetails.getUsername());
        User user = userService.findByTelegramId(tgId);
        UserDto userDto = UserDto.fromEntity(user);
        return ResponseEntity.ok(userDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/role/{role}")
    public ResponseEntity<UserDto> addRoleToUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String role) {
        Long tgId = Long.parseLong(userDetails.getUsername());
        User updatedUser = userService.addRoleToUser(tgId, role);
        return ResponseEntity.ok(UserDto.fromEntity(updatedUser));
    }
}
