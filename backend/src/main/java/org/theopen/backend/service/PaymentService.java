package org.theopen.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.theopen.backend.dto.PaymentRequestDto;
import org.theopen.backend.dto.PaymentResponseDto;
import org.theopen.backend.exception.ConfigNotFoundException;
import org.theopen.backend.exception.PaymentCreationException;
import org.theopen.backend.model.Config;
import org.theopen.backend.repo.ConfigRepository;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final ConfigRepository configRepository;
    private final RestTemplate restTemplate;

    @Value("${tinkoff.terminalKey}")
    private String terminalKey;

    @Value("${tinkoff.secretKey}")
    private String secretKey;

    @Value("${tinkoff.apiUrl}")
    private String tinkoffApiUrl;

    public PaymentResponseDto createPaymentLink(PaymentRequestDto request) {
        Config config = configRepository.findById(request.getConfigId())
                .orElseThrow(ConfigNotFoundException::new);

        Map<String, Object> body = new HashMap<>();
        body.put("TerminalKey", terminalKey);
        body.put("Amount", request.getAmount());
        body.put("OrderId", "VPN-" + config.getId());
        body.put("Description", request.getDescription());
        body.put("SuccessURL", "https://yourapp.com/payment/success");
        body.put("NotificationURL", "https://yourapp.com/api/payment/notify");

        String token = generateToken(body);
        body.put("Token", token);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            tinkoffApiUrl + "/Init", body, Map.class
        );

        Map<String, Object> resp = response.getBody();
        if (resp != null && Boolean.TRUE.equals(resp.get("Success"))) {
            String paymentUrl = (String) resp.get("PaymentURL");
            String paymentId = (String) resp.get("PaymentId");

            config.setPaymentLink(paymentUrl);
            config.setPaymentStatus("pending");
            configRepository.save(config);

            return new PaymentResponseDto(paymentUrl, paymentId);
        } else {
            throw new PaymentCreationException();
        }
    }

    public boolean processTinkoffCallback(String payload, Map<String, String> headers) {
        // 🔒 Validate Tinkoff signature if needed

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = mapper.readValue(payload, Map.class);

            String orderId = (String) data.get("OrderId"); // ex: VPN-123
            String status = (String) data.get("Status");

            Long configId = Long.parseLong(orderId.replace("VPN-", ""));
            Config config = configRepository.findById(configId)
                    .orElseThrow(ConfigNotFoundException::new);

            if ("CONFIRMED".equalsIgnoreCase(status)) {
                config.setPaymentStatus("paid");
                config.setIsActive(true);
            } else {
                config.setPaymentStatus(status.toLowerCase());
            }

            configRepository.save(config);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getPaymentStatus(Long configId) {
        return configRepository.findById(configId)
                .map(Config::getPaymentStatus)
                .orElse("unknown");
    }

    private String generateToken(Map<String, Object> params) {
        // Порядок полей имеет значение
        String data = terminalKey + params.get("Amount") + params.get("OrderId") + secretKey;
        return DigestUtils.sha256Hex(data);
    }
}

