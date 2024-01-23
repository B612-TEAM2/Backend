package com.b6122.ping.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FriendDto {

    private MultipartFile multipartFile;
    private String nickname;

    public FriendDto(MultipartFile multipartFile, String nickname) {
        this.multipartFile = multipartFile;
        this.nickname = nickname;
    }
}
