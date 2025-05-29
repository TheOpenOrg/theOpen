package org.theopen.backend.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
public class BuyConfigRequest {
    @NotNull(message = "serverId is required")
    private Long serverId;
    @NotNull(message = "tgId is required")
    private Long tgId;
    @Min(value = 1, message = "monthAmount must be at least 1")
    private int monthAmount;
}

