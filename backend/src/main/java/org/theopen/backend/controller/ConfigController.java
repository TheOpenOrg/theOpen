package org.theopen.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.theopen.backend.dto.BuyConfigRequest;
import org.theopen.backend.dto.ConfigDto;
import org.theopen.backend.service.ConfigService;
import org.theopen.backend.service.UserService;
import org.theopen.backend.exception.UserNotFoundException;
import org.theopen.backend.dto.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.CacheControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(ConfigController.class);

    /**
     * Получает список конфигураций пользователя
     *
     * @param tgId идентификатор пользователя из заголовка
     * @param authToken токен авторизации
     * @param request объект запроса дл�� получения IP-адреса
     * @return список конфигураций пользователя
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyConfigs(
            @RequestHeader(value = "X-Tg-Id", required = true) Long tgId,
            @RequestHeader(value = "Authorization", required = false) String authToken,
            HttpServletRequest request) {

        // Проверяем, что ID не пустой и положительный
        if (tgId == null || tgId <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Некорректный идентификатор пользователя"));
        }

        try {
            // Проверяем авторизацию - это можно реализовать через Spring Security или вручную
            if (!userService.validateUserAccess(tgId, authToken)) {
                // Логируем попытку несанкционированного доступа
                log.warn("Попытка несанкционированного доступа к конфигурациям. TG ID: {}, IP: {}",
                        tgId, request.getRemoteAddr());

                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Доступ запрещен"));
            }

            // Логируем успешный запрос
            log.info("Запрос списка конфигураций пользователем с TG ID: {}", tgId);

            // Получаем конфигурации и применяем лимит на максимальное количество возвращаемых записей
            List<ConfigDto> configs = configService.getConfigsForUser(tgId)
                    .stream()
                    .limit(100) // Ограничиваем количество возвращаемых конфигураций
                    .map(this::sanitizeConfigData) // Удаляем чувствительную информацию
                    .collect(Collectors.toList());

            // Возвращаем результат с контролем кеширования
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore().mustRevalidate())
                    .header("Pragma", "no-cache")
                    .body(configs);

        } catch (UserNotFoundException e) {
            log.warn("Попытка доступа с несуществующим TG ID: {}", tgId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Пользователь не найден"));
        } catch (Exception e) {
            log.error("Ошибка при получении конфигураций пользователя с TG ID: {}: {}", tgId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Произошла ошибка при обработке запроса"));
        }
    }

    /**
     * Метод для удаления чувствительной информации из конфигурации перед отправкой клиенту
     */
    private ConfigDto sanitizeConfigData(ConfigDto config) {
        // Здесь можно удалить/скрыть чувствительные данные, если они есть в DTO
        // Например, можно скрыть часть информации о сервере или удалить токены и т.п.
        return config;
    }

    @PostMapping("/buy")
    public ResponseEntity<ConfigDto> buyConfig(@Valid @RequestBody BuyConfigRequest request) {
        ConfigDto config = configService.createConfig(request);
        return ResponseEntity.ok(config);
    }

    @PostMapping("/renew/{id}")
    public ResponseEntity<ConfigDto> renew(@PathVariable UUID id, @RequestParam int months) {
        ConfigDto config = configService.renewConfig(id, months);
        return ResponseEntity.ok(config);
    }

    @PostMapping("/trial")
    public ResponseEntity<ConfigDto> getTrial(@RequestParam Long tgId) {
        ConfigDto config = configService.getTrialConfig(tgId);
        return ResponseEntity.ok(config);
    }

    @PostMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        configService.deactivateConfig(id);
        return ResponseEntity.ok().build();
    }
}

