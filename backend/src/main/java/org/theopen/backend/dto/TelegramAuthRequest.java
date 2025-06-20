package org.theopen.backend.dto;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TelegramAuthRequest {
    private Long id;
    private String first_name;
    private String last_name;
    private String username;
    private Boolean allows_write_to_pm;
    private String photo_url;
    private String language_code;
}