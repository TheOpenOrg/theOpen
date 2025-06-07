package org.theopen.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.theopen.backend.model.Server;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerDto {
    private CountryDto country;

    public static ServerDto fromEntity(Server server) {
        ServerDto dto = new ServerDto();
        // Если есть связь с Country, используем её, иначе берём строку country
        if (server.getCountryEntity() != null) {
            dto.setCountry(CountryDto.fromEntity(server.getCountryEntity()));
        } else if (server.getCountry() != null) {
            // Используем строку как имя страны, но без code (флаг не будет работать)
            CountryDto countryDto = new CountryDto();
            countryDto.setName(server.getCountry());
            dto.setCountry(countryDto);
        }

        return dto;
    }
}

