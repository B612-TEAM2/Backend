package com.b6122.ping.dto;

import com.b6122.ping.domain.UserRole;
import lombok.Data;

@Data
public class UserDto {
    Long id;
    String provider;
    String providerId;
    UserRole role;

    public UserDto(Long id, String provider, String providerId, UserRole role) {
        this.id = id;
        this.provider = provider;
        this.providerId = providerId;
        this.role = role;
    }
}
