package com.b6122.ping.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UserProfileReqDto {

    private String nickname;
    private MultipartFile profileImg;
    private Long id;
}
