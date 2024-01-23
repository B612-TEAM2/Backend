package com.b6122.ping.dto;

import com.b6122.ping.domain.UserRole;
import lombok.Data;

@Data
public class UserDto {
    Long id;
    String provider;
    String providerId;
    String username;

    public UserDto(Long id, String provider, String providerId, String username) {
        this.id = id;
        this.provider = provider;
        this.providerId = providerId;
        this.username = username;
    }
}
