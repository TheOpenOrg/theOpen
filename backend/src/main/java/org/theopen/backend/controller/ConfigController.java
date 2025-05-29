package org.theopen.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.theopen.backend.dto.BuyConfigRequest;
import org.theopen.backend.dto.ConfigDto;
import org.theopen.backend.service.ConfigService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    @GetMapping("/my")
    public ResponseEntity<List<ConfigDto>> getMyConfigs(@RequestHeader("X-Tg-Id") Long tgId) {
        List<ConfigDto> configs = configService.getConfigsForUser(tgId);
        return ResponseEntity.ok(configs);
    }

    @PostMapping("/buy")
    public ResponseEntity<ConfigDto> buyConfig(@Valid @RequestBody BuyConfigRequest request) {
        ConfigDto config = configService.createConfig(request);
        return ResponseEntity.ok(config);
    }

    @PostMapping("/renew/{id}")
    public ResponseEntity<ConfigDto> renew(@PathVariable Long id, @RequestParam int months) {
        ConfigDto config = configService.renewConfig(id, months);
        return ResponseEntity.ok(config);
    }

    @PostMapping("/trial")
    public ResponseEntity<ConfigDto> getTrial(@RequestParam Long tgId) {
        ConfigDto config = configService.getTrialConfig(tgId);
        return ResponseEntity.ok(config);
    }

    @PostMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        configService.deactivateConfig(id);
        return ResponseEntity.ok().build();
    }
}

