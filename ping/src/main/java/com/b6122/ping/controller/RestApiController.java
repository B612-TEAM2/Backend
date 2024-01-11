package com.b6122.ping.controller;

import com.b6122.ping.auth.PrincipalDetails;
import com.b6122.ping.domain.User;
import com.b6122.ping.dto.CreateJwtRequestDto;
import com.b6122.ping.dto.UserDto;
import com.b6122.ping.service.JwtService;
import com.b6122.ping.service.OAuthService;
import com.b6122.ping.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequiredArgsConstructor
public class RestApiController {

    private final JwtService jwtService;
    private final UserService userService;
    private final OAuthService oAuthService;

    @PostMapping("/oauth/jwt/kakao")
    public ResponseEntity<Map<String, String>> createJwt(@RequestBody Map<String, String> request) throws IOException {
        //카카오 서버에서 access Token 받기
        String accessToken = oAuthService.getKakaoAccessToken(request.get("code"));

        //카카오 서버로부터 받은 access token으로 카카오 유저 정보 가져오기

        //회원가입

        //jwt accessToken을 리액트 서버에 return
//        return ResponseEntity.ok().body(jwtService.createJwtAccessToken(userDto));

        Map<String, String> responseData = new HashMap<>();
        responseData.put("key", "1234");

        return ResponseEntity.ok().body(responseData);
    }
}
