package com.b6122.ping.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FriendDto {

    private String profileImagePath;
    private String nickname;

    public FriendDto(String profileImagePath, String nickname) {
        this.profileImagePath = profileImagePath;
        this.nickname = nickname;
    }
}
