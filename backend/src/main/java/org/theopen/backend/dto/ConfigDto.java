package org.theopen.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.theopen.backend.model.Config;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigDto {
    private Long id;
    private Long serverId;
    private Long uid;
    private int monthAmount;
    private LocalDateTime buyTime;
    private boolean isActive;
    private String paymentStatus;

    public static ConfigDto fromEntity(Config config) {
        return new ConfigDto(
            config.getId(),
            config.getServer().getId(),
            config.getUser().getId(),
            config.getMonthAmount(),
            config.getBuyTime(),
            config.getIsActive(),
            config.getPaymentStatus()
        );
    }
}