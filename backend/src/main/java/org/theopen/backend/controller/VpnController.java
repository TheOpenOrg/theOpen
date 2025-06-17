package org.theopen.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.theopen.backend.dto.ConfigsRequestDto;
import org.theopen.backend.dto.VpnConfigResponseDto;
import org.theopen.backend.model.Payment;
import org.theopen.backend.service.PaymentService;
import org.theopen.backend.service.VpnService;

import java.util.*;

@RestController
@RequestMapping("/api/vpn")
@RequiredArgsConstructor
@Slf4j
public class VpnController {

    private final VpnService vpnService;
    private final PaymentService paymentService;

    /**
     * Получает конфигурационный файл OpenVPN для указанного клиента
     *
     * @param clientName имя клиента
     * @return объект с результатом операции или файл конфигурации
     */
    @GetMapping("/config/{clientName}")
    public ResponseEntity<?> getClientConfig(@PathVariable String clientName,
                                            @RequestParam(required = false, defaultValue = "false") boolean download) {
        log.info("Getting VPN config for client: {}", clientName);
        VpnConfigResponseDto response = vpnService.getClientConfig(clientName);

        if ("success".equals(response.getStatus()) && download && response.getConfigFile() != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.getFileName() + "\"");

            ByteArrayResource resource = new ByteArrayResource(response.getConfigFile());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(response.getConfigFile().length)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Блокирует клиента OpenVPN
     *
     * @param clientName имя клиента
     * @return объект с результатом операции
     */
    @PostMapping("/block/{clientName}")
    public ResponseEntity<VpnConfigResponseDto> blockClient(@PathVariable String clientName) {
        log.info("Blocking VPN client: {}", clientName);
        VpnConfigResponseDto response = vpnService.blockClient(clientName);
        return ResponseEntity.ok(response);
    }

    /**
     * Разблокирует клиента OpenVPN
     *
     * @param clientName имя клиента
     * @return объект с результатом операции
     */
    @DeleteMapping("/block/{clientName}")
    public ResponseEntity<VpnConfigResponseDto> unblockClient(@PathVariable String clientName) {
        log.info("Unblocking VPN client: {}", clientName);
        VpnConfigResponseDto response = vpnService.unblockClient(clientName);
        return ResponseEntity.ok(response);
    }

    /**
     * Создает новую конфигурацию OpenVPN для клиента
     *
     * @param clientName имя клиента
     * @return объект с результатом операции
     */
    @PostMapping("/config/{clientName}")
    public ResponseEntity<VpnConfigResponseDto> createClientConfig(@PathVariable String clientName) {
        log.info("Creating VPN config for client: {}", clientName);
        VpnConfigResponseDto response = vpnService.createClientConfig(clientName);
        return ResponseEntity.ok(response);
    }

    /**
     * Создает несколько новых конфигураций OpenVPN на оптимальном сервере выбранной страны
     *
     * @param countryId идентификатор страны
     * @param months количество месяцев
     * @param configsCount количество конфигураций
     * @return список с результатами операций создания конфигураций
     */
    @GetMapping("/configs")
    public ResponseEntity<Void> createMultipleConfigs(
            @RequestParam Long countryId,
            @RequestParam Integer months,
            @RequestParam Integer configsCount,
            @RequestParam(required = false) Long telegramId
    ) {
        log.info("Creating multiple VPN configs: {} for country ID: {} for {} months", configsCount, countryId, months);
        ConfigsRequestDto request = new ConfigsRequestDto();
        request.setCountryId(countryId);
        request.setMonths(months);
        request.setConfigsCount(configsCount);
        // 1. Создаем конфиги и получаем paymentUrl
        Optional<Payment> payment = vpnService.createMultipleConfigs(request);

        if (payment.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Payment> paymentQr = paymentService.createQr(payment);
        return paymentQr.<ResponseEntity<Void>>map(value ->
                ResponseEntity.status(302).header("Location", value.getUrl()).build()
        ).orElseGet(() -> ResponseEntity.badRequest().build());

    }

    /**
     * Получает количество активных пользователей OpenVPN
     *
     * @return объект с результатом операции
     */
    @GetMapping("/active-users")
    public ResponseEntity<VpnConfigResponseDto> getActiveUsers() {
        log.info("Getting active VPN users count");
        VpnConfigResponseDto response = vpnService.getActiveUsers();
        return ResponseEntity.ok(response);
    }
}
