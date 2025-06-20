package org.theopen.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();
    private String requestId;
    private int code;

    public ErrorResponse(String message) {
        this.message = message;
    }

    public ErrorResponse(String message, int code) {
        this.message = message;
        this.code = code;
    }
}
