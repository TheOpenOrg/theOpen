package org.theopen.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VpnConfigResponseDto {
    private String status;
    private String error;
    private Integer activeUsers;
    private String message;
    private byte[] configFile;
    private String fileName;
}
