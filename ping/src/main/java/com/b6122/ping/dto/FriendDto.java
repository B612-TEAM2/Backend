package com.b6122.ping.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FriendDto {

    private Long id;
    private byte[] imageBytes;
    private String nickname;

    public FriendDto(Long id, byte[] imageBytes, String nickname) {
        this.id = id;
        this.imageBytes = imageBytes;
        this.nickname = nickname;
    }
}
