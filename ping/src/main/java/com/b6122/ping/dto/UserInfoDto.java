package com.b6122.ping.dto;

import lombok.Data;

@Data
public class UserInfoDto {

    private String nickname;
    private byte[] imageBytes;

    public UserInfoDto(String nickname, byte[] imageBytes) {
        this.nickname = nickname;
        this.imageBytes = imageBytes;
    }
}
