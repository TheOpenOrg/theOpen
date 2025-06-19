package org.theopen.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.theopen.backend.model.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long tgId;
    private String name;

    public static UserDto fromEntity(User user) {
        return new UserDto(user.getTgId(), user.getName());
    }
}

