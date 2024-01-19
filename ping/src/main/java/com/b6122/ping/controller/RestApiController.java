package com.b6122.ping.controller;

import com.b6122.ping.dto.UserDto;
import com.b6122.ping.service.JwtService;
import com.b6122.ping.service.KakaoOAuthService;
import com.b6122.ping.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;


@RestController
@RequiredArgsConstructor
public class RestApiController {

    private final JwtService jwtService;
    private final UserService userService;
    private final KakaoOAuthService oAuthService;

    @PostMapping("/oauth/jwt/kakao")
    public ResponseEntity<Map<String, String>> createJwt(@RequestBody Map<String, Object> request) throws IOException {
        // 프론트엔드로부터 authorization code 받고 -> 그 code로 카카오에 accesstoken 요청
        String accessToken = oAuthService.getKakaoAccessToken(request.get("code").toString());

        // 받아 온 access token으로 카카오 리소스 서버로부터 카카오 유저 정보 가져오기
        Map<String, Object> userInfo = oAuthService.getKakaoUserInfo(accessToken);

        // 가져온 정보를 기반으로 회원가입
        UserDto userDto = userService.joinOAuthUser(userInfo);

        // jwt accessToken을 리액트 서버에 return
        return ResponseEntity.ok().body(jwtService.createJwtAccessToken(userDto));
    }
}
