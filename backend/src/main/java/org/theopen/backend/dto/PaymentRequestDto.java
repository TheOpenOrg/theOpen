package org.theopen.backend.dto;

import lombok.Data;

@Data
public class PaymentRequestDto {
    private Long configId;
    private Long amount; // в копейках
    private String description;
}