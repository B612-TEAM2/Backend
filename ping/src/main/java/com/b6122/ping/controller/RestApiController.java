package com.b6122.ping.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.b6122.ping.auth.PrincipalDetails;
import com.b6122.ping.dto.UserDto;
import com.b6122.ping.service.JwtService;
import com.b6122.ping.service.KakaoOAuthService;
import com.b6122.ping.service.UserService;
import com.b6122.ping.service.GoogleOAuthService;
import com.b6122.ping.service.NaverOAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
public class RestApiController {

    private final NaverOAuthService naverOAuthService;
    private final GoogleOAuthService googleOAuthService;
    private final JwtService jwtService;
    private final UserService userService;
    private final KakaoOAuthService kakaoOAuthService;

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

    @CrossOrigin(origins = "https://localhost:3000/authnaver")
    @PostMapping("/oauth/jwt/naver")
    public ResponseEntity<Map<String, String>> createJwtNaver(@RequestBody Map<String, Object> request) throws IOException {
        // Frontend sends the authorization code, use it to request Naver for an access token
        String accessToken = naverOAuthService.getNaverAccessToken(request.get("code").toString());

        // Use the obtained access token to fetch Naver user information from Naver resource server
        Map<String, Object> userInfo = naverOAuthService.getNaverUserInfo(accessToken);

        // Based on the retrieved information, perform user registration
        UserDto userDto = userService.joinOAuthUser(userInfo);

        // Return the JWT access token to the React server
        return ResponseEntity.ok().body(jwtService.createJwtAccessToken(userDto));
    }

    //친구 목록
    @GetMapping("/friends/list")
    public ResponseEntity<Map<String, Object>> friends(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();;
        List<UserDto> friendsList = userService.findFriends(principalDetails.getUser().getId());

        Map<String, Object> data = new HashMap<>();
        data.put("friendsList", friendsList);
        return ResponseEntity.ok().body(data);
    }

}
