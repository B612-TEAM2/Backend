package com.b6122.ping.dto;

import lombok.Data;

@Data
public class UserProfileDto {

    private String nickname;
    private byte[] profileImg;
    private Long id;

    public UserProfileDto(Long id, String nickname, byte[] profileImg) {
        this.nickname = nickname;
        this.profileImg = profileImg;
        this.id = id;
    }
}
