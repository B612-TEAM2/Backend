package com.b6122.ping.dto;

import lombok.Data;

@Data
public class UserInfoDto {

    private String nickname;
    private byte[] profileImg;

    public UserInfoDto(String nickname, byte[] profileImg) {
        this.nickname = nickname;
        this.profileImg = profileImg;
    }
}
