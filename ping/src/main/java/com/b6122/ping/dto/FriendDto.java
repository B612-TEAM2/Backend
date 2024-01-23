package com.b6122.ping.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FriendDto {

    private byte[] imageBytes;
    private String nickname;

    public FriendDto(byte[] imageBytes, String nickname) {
        this.imageBytes = imageBytes;
        this.nickname = nickname;
    }
}
