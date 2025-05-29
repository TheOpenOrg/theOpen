package org.theopen.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.theopen.backend.model.Server;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerDto {
    private Long id;
    private String name;
    private String country;

    public static ServerDto fromEntity(Server server) {
        return new ServerDto(
            server.getId(),
            server.getName(),
            server.getCountry()
        );
    }
}