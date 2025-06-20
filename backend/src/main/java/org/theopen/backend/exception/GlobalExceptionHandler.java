package org.theopen.backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.theopen.backend.dto.VpnConfigResponseDto;
import org.theopen.backend.dto.ErrorResponseDto;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<VpnConfigResponseDto> handleHttpServerErrorException(HttpServerErrorException e) {
        log.error("HttpServerErrorException: {}", e.getMessage());
        String userMessage = "Не удалось создать конфигурацию клиента. Попробуйте позже.";
        try {
            String responseBody = e.getResponseBodyAsString();
            if (responseBody.contains("error")) {
                int idx = responseBody.indexOf(":");
                int endIdx = responseBody.lastIndexOf('"');
                if (idx > 0 && endIdx > idx) {
                    userMessage = responseBody.substring(idx + 2, endIdx);
                }
            }
        } catch (Exception ignored) {}
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(VpnConfigResponseDto.builder()
                        .status("error")
                        .error(userMessage)
                        .build());
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<VpnConfigResponseDto> handleResourceAccessException(ResourceAccessException e) {
        // Логируем только сообщение об ошибке без stacktrace
        log.error("Ошибка соединения с VPN-сервером: {}", e.getMessage());

        String errorMessage;
        if (e.getMessage().contains("Connection refused")) {
            errorMessage = "Не удалось подключиться к VPN-серверу. Сервер временно недоступен. Попробуйте позже.";
        } else if (e.getMessage().contains("timed out")) {
            errorMessage = "Превышено время ожидания ответа от VPN-сервера. Попробуйте позже.";
        } else if (e.getMessage().contains("Connection reset")) {
            errorMessage = "Соединение с VPN-сервером было сброшено. Попробуйте позже.";
        } else {
            errorMessage = "Проблема с доступом к VPN-серверу. Попробуйте позже.";
        }

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(VpnConfigResponseDto.builder()
                        .status("error")
                        .error(errorMessage)
                        .build());
    }

    @ExceptionHandler(VpnConfigException.class)
    public ResponseEntity<VpnConfigResponseDto> handleVpnConfigException(VpnConfigException e) {
        log.error("VPN Configuration Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(VpnConfigResponseDto.builder()
                        .status("error")
                        .error(e.getMessage())
                        .build());
    }

    @ExceptionHandler(TelegramAuthException.class)
    public ResponseEntity<VpnConfigResponseDto> handleAuthException(VpnConfigException e) {
        log.error("Auth Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(VpnConfigResponseDto.builder()
                        .status("error")
                        .error(e.getMessage())
                        .build());
    }

    @ExceptionHandler(ServerApiException.class)
    public ResponseEntity<VpnConfigResponseDto> handleServerApiException(ServerApiException e) {
        log.error("Server API Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(VpnConfigResponseDto.builder()
                        .status("error")
                        .error("Ошибка взаимодействия с VPN-сервером. Попробуйте позже.")
                        .build());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(UserNotFoundException e) {
        log.error("UserNotFoundException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto("Пользователь не найден"));
    }

    @ExceptionHandler(ServerNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleServerNotFoundException(ServerNotFoundException e) {
        log.error("ServerNotFoundException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto("Сервер не найден"));
    }

    @ExceptionHandler(ConfigNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleConfigNotFoundException(ConfigNotFoundException e) {
        log.error("ConfigNotFoundException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto("Конфигурация не найдена"));
    }

    @ExceptionHandler(InactiveConfigException.class)
    public ResponseEntity<ErrorResponseDto> handleInactiveConfigException(InactiveConfigException e) {
        log.error("InactiveConfigException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("Конфигурация неактивна"));
    }

    @ExceptionHandler(TrialAlreadyUsedException.class)
    public ResponseEntity<ErrorResponseDto> handleTrialAlreadyUsedException(TrialAlreadyUsedException e) {
        log.error("TrialAlreadyUsedException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("Пробный период уже использован"));
    }

    @ExceptionHandler(PaymentCreationException.class)
    public ResponseEntity<ErrorResponseDto> handlePaymentCreationException(PaymentCreationException e) {
        log.error("PaymentCreationException: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto("Ошибка при создании платежа"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception e) {
        log.error("Exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto("Внутренняя ошибка сервера. Пожалуйста, попробуйте позже."));
    }
}
