package org.theopen.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.theopen.backend.dto.PaymentRequestDto;
import org.theopen.backend.dto.PaymentResponseDto;
import org.theopen.backend.service.PaymentService;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<PaymentResponseDto> createPayment(@RequestBody PaymentRequestDto request) {
        PaymentResponseDto response = paymentService.createPaymentLink(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/notify")
    public ResponseEntity<String> handleTinkoffCallback(@RequestBody String payload,
                                                         @RequestHeader Map<String, String> headers) {
        boolean success = paymentService.processTinkoffCallback(payload, headers);
        return success ? ResponseEntity.ok("OK") : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid");
    }

    @GetMapping("/status/{configId}")
    public ResponseEntity<String> getPaymentStatus(@PathVariable Long configId) {
        String status = paymentService.getPaymentStatus(configId);
        return ResponseEntity.ok(status);
    }
}