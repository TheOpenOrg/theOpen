package org.theopen.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigsRequestDto {
    private Long countryId; // ID страны
    private Integer months; // Количество месяцев
    private Integer configsCount; // Количество конфигураций
    private Long telegramId;
}
