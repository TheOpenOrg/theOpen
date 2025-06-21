package org.theopen.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.theopen.backend.model.User;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long tgId;
    private String name;
    private Set<String> roles;
    private String accessToken;

    public static UserDto fromEntity(User user) {
        UserDto dto = new UserDto();
        dto.setTgId(user.getTgId());
        dto.setName(user.getName());
        dto.setRoles(user.getRoles());
        return dto;
    }
}

