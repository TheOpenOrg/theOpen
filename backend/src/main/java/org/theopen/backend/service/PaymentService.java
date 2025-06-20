package org.theopen.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.theopen.backend.dto.PaymentRequestDto;
import org.theopen.backend.model.Config;
import org.theopen.backend.model.Payment;
import org.theopen.backend.repo.ConfigRepository;
import org.theopen.backend.repo.PaymentRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ConfigRepository configRepository;
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;

    @Value("${tinkoff.terminalKey}")
    private String terminalKey;

    @Value("${tinkoff.secretKey}")
    private String secretKey;

    @Value("${tinkoff.apiUrl}")
    private String tinkoffApiUrl;

    public Optional<Payment> createPaymentLink(PaymentRequestDto request) {
        Map<String, Object> body = new HashMap<>();
        body.put("TerminalKey", terminalKey);
        body.put("Amount", request.getAmount());
        // Генерируем короткий уникальный orderId
        String orderId = "theOpen-" + System.currentTimeMillis() + "-" + (int)(Math.random()*1000);
        body.put("OrderId", orderId);
        body.put("Description", request.getDescription());
        body.put("NotificationURL", "https://organic-fortnight-4wwqjq74qp527477-8080.app.github.dev/api/payment/notify");

        String token = generateToken(body);
        body.put("Token", token);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                tinkoffApiUrl + "/Init", body, Map.class
        );

        Map<String, Object> resp = response.getBody();
        log.info("Payment link created: {}", resp);
        if (resp != null && Boolean.TRUE.equals(resp.get("Success"))) {
            String paymentUrl = (String) resp.get("PaymentURL");
            String paymentIdStr = String.valueOf(resp.get("PaymentId"));

            Payment payment = new Payment();
            payment.setPaymentId(Long.parseLong(paymentIdStr));
            payment.setTerminalKey(terminalKey);
            payment.setAmount(request.getAmount());
            payment.setOrderId(orderId);
            payment.setDescription(request.getDescription());
            payment.setToken(token);
            payment.setUrl(paymentUrl);
            return Optional.of(paymentRepository.save(payment));
        } else {
            log.error("Failed to create payment: {}", resp);
            return Optional.empty();
        }
    }

    public Optional<Payment> createQr(Optional<Payment> payment) {
        if (payment.isEmpty()) {
            log.error("Payment not found");
            return Optional.empty();
        }
        Map<String, Object> body = new HashMap<>();
        body.put("TerminalKey", payment.get().getTerminalKey());
        body.put("PaymentId", payment.get().getPaymentId());
        body.put("DataType", "PAYLOAD");
        String token = generateToken(body);
        body.put("Token", token);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                tinkoffApiUrl + "/GetQr", body, Map.class
        );
        Map<String, Object> resp = response.getBody();
        log.info("Payment link created: {}", resp);
        if (resp != null && Boolean.TRUE.equals(resp.get("Success"))) {
            Payment payment1 = payment.get();
            payment1.setUrl(resp.get("Data").toString());
            return Optional.of(paymentRepository.save(payment1));
        } else {
            log.error("Failed to create payment: {}", resp);
            return Optional.empty();
        }
    }

    public boolean processTinkoffCallback(String payload, Map<String, String> headers) {
        // 🔒 Validate Tinkoff signature if needed
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = mapper.readValue(payload, Map.class);

            String status = (String) data.get("Status");
            String paymentIdStr = String.valueOf(data.get("PaymentId"));
            Long paymentId = paymentIdStr != null ? Long.parseLong(paymentIdStr) : null;
            List<Config> configList = configRepository.findAllByPaymentPaymentId(paymentId);

            // Обновляем статус платежа
            if (paymentId != null) {
                Payment payment = paymentRepository.findByPaymentId(paymentId);
                if (payment != null) {
                    payment.setDescription(status); // Можно хранить статус в отдельном поле, если нужно
                    paymentRepository.save(payment);
                }
            }

            // Если статус CONFIRMED, активируем все связанные конфиги
            if ("CONFIRMED".equalsIgnoreCase(status)) {
                for (Config c : configList) {
                    c.setIsActive(true);
                }
                configRepository.saveAll(configList);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getPaymentStatus(UUID configId) {
        Config config = configRepository.findById(configId).orElse(null);
        if (config == null || config.getPayment() == null) return "unknown";
        Payment payment = config.getPayment();
        return payment.getStatus();
    }

    /**
     * Отменяет платеж, вызывая Tinkoff API Cancel
     *
     * @param payment объект платежа для отмены
     * @return true если отмена прошла успешно, false в противном случае
     */
    public boolean cancelPayment(Payment payment) {
        if (payment == null) {
            log.error("Cannot cancel null payment");
            return false;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("TerminalKey", terminalKey);
        body.put("PaymentId", payment.getPaymentId());
        String token = generateToken(body);
        body.put("Token", token);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    tinkoffApiUrl + "/Cancel", body, Map.class
            );

            Map<String, Object> resp = response.getBody();
            log.info("Payment cancellation response: {}", resp);

            if (resp != null && Boolean.TRUE.equals(resp.get("Success"))) {
                // Обновляем статус платежа
                payment.setStatus("CANCELED");
                paymentRepository.save(payment);
                return true;
            } else {
                log.error("Failed to cancel payment {}: {}", payment.getPaymentId(), resp);
                return false;
            }
        } catch (Exception e) {
            log.error("Error canceling payment {}: {}", payment.getPaymentId(), e.getMessage());
            return false;
        }
    }

    /**
     * Генерирует токен безопасности для API Tinkoff
     *
     * @param params параметры запроса
     * @return токен безопасности
     */
    private String generateToken(Map<String, Object> params) {
        // Копируем только корневые параметры (без вложенных объектов и массивов)
        Map<String, String> flatParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            if (value == null) continue;
            if (value instanceof String || value instanceof Number || value instanceof Boolean) {
                flatParams.put(entry.getKey(), String.valueOf(value));
            }
        }
        // Добавляем пароль
        flatParams.put("Password", secretKey);
        // Сортируем по ключу
        List<Map.Entry<String, String>> sorted = new ArrayList<>(flatParams.entrySet());
        sorted.sort(Map.Entry.comparingByKey());
        log.info("Sorted params: {}", sorted);
        // Конкатенируем значения
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted) {
            sb.append(entry.getValue());
        }
        String data = sb.toString();
        // SHA-256
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации токена", e);
        }
    }
}

