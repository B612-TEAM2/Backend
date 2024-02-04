package com.b6122.ping.dto;

import lombok.Data;

@Data
public class UserProfileResDto {

    private String nickname;
    private byte[] profileImg;
    private Long id;

    public UserProfileResDto(Long id, String nickname, byte[] profileImg) {
        this.nickname = nickname;
        this.profileImg = profileImg;
        this.id = id;
    }
}
