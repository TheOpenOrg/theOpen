package org.theopen.backend.dto;

import lombok.Data;

@Data
public class TelegramAuthRequest {
    private Long id;
    private String first_name;
    private String last_name;
    private String username;
    private Long auth_date;
    private String hash;
}