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
     * @param configId id конфигурации OpenVPN
     * @param telegramId id пользователя в Telegram
     * @return объект с результатом операции или файл конфигурации
     */
    @GetMapping("/config/{configId}")
    public ResponseEntity<?> getClientConfig(@PathVariable UUID configId,
                                             @RequestParam Long telegramId,
                                             @RequestParam(required = false, defaultValue = "false") boolean download) {
        log.info("Getting VPN config for config: {}", configId);
        VpnConfigResponseDto response = vpnService.getClientConfig(configId, telegramId);

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
     * Создает несколько новых конфигураций OpenVPN на оптимальном сервере выбранной страны
     *
     * @param countryId    идентификатор страны
     * @param months       количество месяцев
     * @param configsCount количество конфигураций
     * @return список с результатами операций создания конфигураций
     */
    @GetMapping("/configs")
    public ResponseEntity<?> createMultipleConfigs(
            @RequestParam Long countryId,
            @RequestParam Integer months,
            @RequestParam Integer configsCount,
            @RequestParam Long telegramId
    ) {
        log.info("Creating multiple VPN configs: {} for country ID: {} for {} months", configsCount, countryId, months);
        ConfigsRequestDto request = new ConfigsRequestDto();
        request.setCountryId(countryId);
        request.setMonths(months);
        request.setConfigsCount(configsCount);
        request.setTelegramId(telegramId);
        // 1. Создаем конфиги и получаем paymentUrl
        Optional<Payment> payment = vpnService.createMultipleConfigs(request);

        if (payment.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Payment> paymentQr = paymentService.createQr(payment);
        if (paymentQr.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("paymentUrl", paymentQr.get().getUrl());
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Не удалось создать QR-код для оплаты");
            return ResponseEntity.badRequest().body(response);
        }
    }

}
