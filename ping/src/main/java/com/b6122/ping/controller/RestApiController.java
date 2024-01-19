package com.b6122.ping.controller;

import com.b6122.ping.dto.UserDto;
import com.b6122.ping.service.JwtService;
import com.b6122.ping.service.KakaoOAuthService;
import com.b6122.ping.service.UserService;
import com.b6122.ping.service.GoogleOAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final KakaoOAuthService kakaoOAuthService;
    private final GoogleOAuthService googleOAuthService;

    @PostMapping("/oauth/jwt/kakao")
    public ResponseEntity<Map<String, String>> createJwt(@RequestBody Map<String, Object> request) throws IOException {
        // 프론트엔드로부터 authorization code 받고 -> 그 code로 카카오에 accesstoken 요청
        String accessToken = kakaoOAuthService.getKakaoAccessToken(request.get("code").toString());

        // 받아 온 access token으로 카카오 리소스 서버로부터 카카오 유저 정보 가져오기
        Map<String, Object> userInfo = kakaoOAuthService.getKakaoUserInfo(accessToken);

        // 가져온 정보를 기반으로 회원가입
        UserDto userDto = userService.joinOAuthUser(userInfo);

        // jwt accessToken을 리액트 서버에 return
        return ResponseEntity.ok().body(jwtService.createJwtAccessToken(userDto));
    }
  
    @PostMapping("/oauth/jwt/google")
    public ResponseEntity<Map<String, String>> createGoogleJwt(@RequestBody Map<String, Object> request) throws IOException {
        // Frontend sends the authorization code to the server
        String authorizationCode = request.get("code").toString();

        // Exchange the authorization code for an access token from Google
        String accessToken = googleOAuthService.getGoogleAccessToken(authorizationCode);

        // Use the access token to fetch user information from Google
        Map<String, Object> userInfo = googleOAuthService.getGoogleUserInfo(accessToken);

        // Process the user information and perform user registration if needed
        UserDto userDto = userService.joinOAuthUser(userInfo);

        // Return the JWT access token to the React server
        return ResponseEntity.ok().body(jwtService.createJwtAccessToken(userDto));
    }

}
