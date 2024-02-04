package com.b6122.ping.controller;

import com.b6122.ping.auth.PrincipalDetails;
import com.b6122.ping.domain.Friendship;
import com.b6122.ping.dto.UserDto;
import com.b6122.ping.dto.UserProfileReqDto;
import com.b6122.ping.dto.UserProfileResDto;
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

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
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



    @PostMapping("/profile")
    public void setInitialProfile(@RequestParam("profileImg") MultipartFile profileImg,
                                  @RequestParam("nickname") String nickname,
                                  Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        UserProfileReqDto reqDto = new UserProfileReqDto();
        reqDto.setId(principalDetails.getUser().getId());
        reqDto.setNickname(nickname);
        reqDto.setProfileImg(profileImg);

        userService.updateProfile(reqDto);
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
        UserProfileResDto resDto = userService.getUserProfile(principalDetails.getUser().getId());

        Map<String, Object> data = new HashMap<>();
        data.put("nickname", resDto.getNickname());
        data.put("profileImg", resDto.getProfileImg());
        data.put("id", resDto.getId());

        return ResponseEntity.ok().body(data);
    }

    //회원정보 변경(일단 사진만, 닉네임까지 확장 가능)
    @PostMapping("/account")
    public void updateProfileImage(@RequestBody UserProfileReqDto reqDto,
                                   Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        reqDto.setId(principalDetails.getUser().getId());
        userService.updateProfile(reqDto);
    }


    /**
     * 친구 목록 불러오기 (자기 친구)
     * @param authentication
     * @return
     */
    @GetMapping("/friends")
    public ResponseEntity<Map<String, Object>> getFriendsList(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        List<UserProfileResDto> result = friendshipService.findFriendsById(principalDetails.getUser().getId());
        Map<String, Object> data = new HashMap<>();
        data.put("list", result);
        return ResponseEntity.ok().body(data);
    }

    /**
     * 친구삭제
     *
     * @param request {"nickname" : "xxx"}
     */
    @DeleteMapping("/friends")
    public void deleteFriend(@RequestBody Map<String, Object> request, Authentication authentication) {
        String friendNickname = request.get("nickname").toString();
        UserProfileResDto findUserDto = userService.findUserByNickname(friendNickname);
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
            UserProfileResDto resDto = userService.findUserByNickname(nickname);
            Optional<Friendship> friendship =
                    friendshipService.findFriendByIds(principalDetails.getUser().getId(), resDto.getId());
            data.put("nickname", resDto.getNickname());
            data.put("profileImg", resDto.getProfileImg());
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
        //fromUserId -> 친구 신청한 사람 id, toUserId -> 친구 신청 상대방 id
        friendshipService.sendRequest(fromUserId, toUserId);
    }


    //친구 요청 온 거 리스트
    @GetMapping("/friends/pending")
    public ResponseEntity<Map<String, Object>> friendsRequestList(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        List<UserProfileResDto> list = friendshipService.findPendingFriendsToMe(principalDetails.getUser().getId());
        Map<String, Object> data = new HashMap<>();
        data.put("data", list);
        return ResponseEntity.ok().body(data);
    }

    //친구 요청 수락
    @PostMapping("/friends/pending")
    public void addFriend(Authentication authentication, @RequestParam("nickname") String nickname) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Long toUserId = principalDetails.getUser().getId();

        UserProfileResDto findUserDto = userService.findUserByNickname(nickname);
        Long fromUserId = findUserDto.getId();

        //toUserId -> 친구 요청을 받은 유저
        //fromUserId -> 친구 요청을 보낸 유저
        friendshipService.addFriend(toUserId, fromUserId);

    }
}
