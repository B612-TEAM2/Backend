package com.b6122.ping.controller;

import com.b6122.ping.auth.PrincipalDetails;
import com.b6122.ping.dto.FriendDto;
import com.b6122.ping.dto.UserDto;
import com.b6122.ping.dto.UserInfoDto;
import com.b6122.ping.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    private final FriendshipService friendshipService;

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

    @CrossOrigin
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


    //처음 소셜 로그인 후 바로 다음에 닉네임, 프로필 사진 설정 메소드
    @PostMapping("/nickname")
    public void setInitialProfile(@RequestParam("profileImg") MultipartFile file,
                            @RequestParam("nickname") String nickname,
                            Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Long userId = principalDetails.getUser().getId();
        userService.updateProfile(file, nickname, userId);

    }

    //친구 목록
    @GetMapping("/friends")
    public ResponseEntity<Map<String, Object>> friends(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        List<FriendDto> friendsList = userService.findFriendsById(principalDetails.getUser().getId());

        Map<String, Object> data = new HashMap<>();
        data.put("friendsList", friendsList);
        return ResponseEntity.ok().body(data);
    }

    //회원 탈퇴
    @DeleteMapping("/account")
    public void deleteAccount(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        userService.deleteAccount(principalDetails.getUser().getId());
    }

    //사용자 정보(닉네임, 사진) 가져오기
    @GetMapping("/account")
    public ResponseEntity<Map<String, Object>> account(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        UserInfoDto userInfoDto = userService.userWithNicknameAndImage(principalDetails.getUser().getId());
        Map<String, Object> data = new HashMap<>();
        data.put("userInfo", userInfoDto);

        return ResponseEntity.ok().body(data);
    }

    //회원정보 변경(일단 사진만, 닉네임까지 확장 가능)
    @PostMapping("/account")
    public void updateProfileImage(@RequestParam("profileImg") MultipartFile file,
                                   Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        userService.updateProfile(file,
                principalDetails.getUser().getNickname(),
                principalDetails.getUser().getId());
    }

    //친구 삭제
    @DeleteMapping("/friends")
    public void deleteFriend(@RequestBody Map<String, Object> request, Authentication authentication) {
        String friendNickname = request.get("nickname").toString();
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Long userId = principalDetails.getUser().getId();

        friendshipService.deleteFriend(friendNickname, userId);

    }
}
