package org.theopen.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.theopen.backend.model.Country;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CountryDto {
    private Long id;
    private String name;
    private String code;
    private String nameRu;

    public static CountryDto fromEntity(Country country) {
        if (country == null) {
            return null;
        }
        return new CountryDto(
                country.getId(),
                country.getName(),
                country.getCode(),
                country.getNameRu()
        );
    }
}
