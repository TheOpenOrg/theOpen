package org.theopen.backend.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.theopen.backend.service.PaymentService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    @PostMapping("/notify")
    public ResponseEntity<String> handleTinkoffCallback(@RequestBody String payload,
                                                         @RequestHeader Map<String, String> headers) {
        logger.info("Tinkoff notify request body: {}", payload);
        boolean success = paymentService.processTinkoffCallback(payload, headers);
        return success ? ResponseEntity.ok("OK") : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid");
    }

    @GetMapping("/status/{configId}")
    public ResponseEntity<String> getPaymentStatus(@PathVariable UUID configId) {
        String status = paymentService.getPaymentStatus(configId);
        return ResponseEntity.ok(status);
    }
}
