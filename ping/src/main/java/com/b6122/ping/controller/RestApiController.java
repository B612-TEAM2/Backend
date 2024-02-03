package com.b6122.ping.controller;

import com.b6122.ping.auth.PrincipalDetails;
import com.b6122.ping.domain.Friendship;
import com.b6122.ping.dto.UserDto;
import com.b6122.ping.dto.UserProfileDto;
import com.b6122.ping.service.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
public class RestApiController {

    private final GoogleOAuthService googleOAuthService;
    private final JwtService jwtService;
    private final UserService userService;
    private final KakaoOAuthService kakaoOAuthService;
    private final FriendshipService friendshipService;

    //프론트엔드로부터 authorization code 받고 -> 그 code로 카카오에 accesstoken 요청
    // 받아 온 access token으로 카카오 리소스 서버로부터 카카오 유저 정보 가져오기
    // 가져온 정보를 기반으로 회원가입
    // jwt accessToken을 리액트 서버에 return
    @PostMapping("/oauth/jwt/{serverName}")
    public ResponseEntity<Map<String, Object>> oauthLogin(@PathVariable("serverName") String server,
                                                         @RequestBody Map<String, Object> request) throws IOException {
        if ("kakao".equals(server)) {
            String accessToken = kakaoOAuthService.getKakaoAccessToken(request.get("code").toString());
            Map<String, Object> userInfo = kakaoOAuthService.getKakaoUserInfo(accessToken);
            UserDto userDto = userService.joinOAuthUser(userInfo);
            return ResponseEntity.ok().body(jwtService.createJwtAccessAndRefreshToken(userDto));
        } else if ("google".equals(server)) {
            String accessToken = googleOAuthService.getGoogleAccessToken(request.get("code").toString());
            Map<String, Object> userInfo = googleOAuthService.getGoogleUserInfo(accessToken);
            UserDto userDto = userService.joinOAuthUser(userInfo);

            return ResponseEntity.ok().body(jwtService.createJwtAccessAndRefreshToken(userDto));
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("error", "Nothing matches to request");

            return ResponseEntity.badRequest().body(data);
        }
    }


    /**
     * @param file     : 사용자가 업로드한 이미지 파일 (form data)
     * @param nickname : 사용자가 설정한 고유 nickname
     */
    @PostMapping("/profile")
    public void setInitialProfile(@RequestParam("profileImg") MultipartFile file,
                                  @RequestParam("nickname") String nickname,
                                  Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Long userId = principalDetails.getUser().getId();
        userService.updateProfile(file, nickname, userId);
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
        UserProfileDto userInfoDto = userService.getUserProfile(principalDetails.getUser().getId());
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

    /**
     * 친구삭제
     *
     * @param request {"nickname" : "xxx"}
     */
    @DeleteMapping("/friends")
    public void deleteFriend(@RequestBody Map<String, Object> request, Authentication authentication) {
        String friendNickname = request.get("nickname").toString();
        UserProfileDto findUserDto = userService.findUserByNickname(friendNickname);
        Long friendId = findUserDto.getId();

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Long userId = principalDetails.getUser().getId();

        friendshipService.deleteFriend(friendId, userId);

    }


    /**
     * 사용자의 nickname을 검색하여 찾기
     *
     * @param nickname 쿼리 파라미터로 전달
     * @return 사용자 정보(UserProfileDto -> nickname, profileImg), 친구 여부
     */
    @GetMapping("/friends/search")
    public ResponseEntity<Map<String, Object>> searchUser(@RequestParam("nickname") String nickname,
                                                          Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Map<String, Object> data = new HashMap<>();
        try {
            UserProfileDto result = userService.findUserByNickname(nickname);
            Optional<Friendship> friendship =
                    friendshipService.findFriendByIds(principalDetails.getUser().getId(), result.getId());
            data.put("nickname", result.getNickname());
            data.put("profileImg", result.getProfileImg());
            data.put("isFriend", friendship.isPresent());
            return ResponseEntity.ok().body(data);
        } catch (EntityNotFoundException e) {
            data.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(data);
        }

    }

    //친구 신청
    @PostMapping("/friends/search")
    public void sendFriendRequest(Authentication authentication,
                                                                 @RequestParam("id") Long toUserId) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Long fromUserId = principalDetails.getUser().getId();
        //userId는 친구 신청 하는 유저, friendId는 친구 신청 받는 유저
        friendshipService.sendRequest(fromUserId, toUserId);
    }

    //친구 요청 수락
    @PostMapping("/friends/pendinglist")
    public void addFriend(Authentication authentication, @RequestParam("nickname") String nickname) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Long toUserId = principalDetails.getUser().getId();

        UserProfileDto findUserDto = userService.findUserByNickname(nickname);
        Long fromUserId = findUserDto.getId();

        //toUserId -> 친구 요청을 받은 유저
        //fromUserId -> 친구 요청을 보낸 유저
        friendshipService.addFriend(toUserId, fromUserId);

    }
}
