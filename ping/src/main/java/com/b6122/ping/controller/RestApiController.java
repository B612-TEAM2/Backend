package com.b6122.ping.controller;

import com.b6122.ping.auth.PrincipalDetails;
import com.b6122.ping.dto.SearchUserResDto;
import com.b6122.ping.dto.UserDto;
import com.b6122.ping.dto.UserProfileReqDto;
import com.b6122.ping.dto.UserProfileResDto;
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

    private final JwtService jwtService;
    private final UserService userService;
    private final FriendshipService friendshipService;
    private final OauthService oauthService;

    //프론트엔드로부터 authorization code 받고 -> 그 code로 카카오에 accesstoken 요청
    // 받아 온 access token으로 카카오 리소스 서버로부터 카카오 유저 정보 가져오기
    // 가져온 정보를 기반으로 회원가입
    // jwt accessToken을 리액트 서버에 return
    @PostMapping("/oauth/jwt/{serverName}")
    public ResponseEntity<Map<String, Object>> oauthLogin(@PathVariable("serverName") String server,
                                                         @RequestBody Map<String, Object> request) throws IOException {

        UserDto joinedUser = oauthService.join(server, request.get("code").toString());
        return ResponseEntity.ok().body(jwtService.createJwtAccessAndRefreshToken(joinedUser));
    }

    @PostMapping("/profile")
    public void setInitialProfile(@RequestParam("profileImg") MultipartFile profileImg,
                                  @RequestParam("nickname") String nickname,
                                  Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        UserProfileReqDto reqDto = new UserProfileReqDto(nickname, profileImg,
                principalDetails.getUser().getId());
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
    public ResponseEntity<UserProfileResDto> account(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        UserProfileResDto resDto = userService.getUserProfile(principalDetails.getUser().getId());
        return ResponseEntity.ok().body(resDto);
    }

    //회원정보 변경(일단 사진만, 닉네임까지 확장 가능)
    @PostMapping("/account")
    public void updateProfile(@RequestParam("profileImg") MultipartFile profileImg,
                                   @RequestParam("nickname") String nickname,
                                   Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        UserProfileReqDto reqDto = new UserProfileReqDto(nickname, profileImg,
                principalDetails.getUser().getId());
        userService.updateProfile(reqDto);
    }


    /**
     * 친구 목록 불러오기 (자기 친구)
     * @return
     */
    @GetMapping("/friends")
    public ResponseEntity<List<UserProfileResDto>> getFriendsList(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        List<UserProfileResDto> result = friendshipService.getFriendsProfile(principalDetails.getUser().getId());
        return ResponseEntity.ok().body(result);
    }

    /**
     * 친구삭제
     * @param request {"nickname" : "xxx"}
     */
    @DeleteMapping("/friends")
    public void deleteFriend(@RequestBody Map<String, Object> request, Authentication authentication) {
        String friendNickname = request.get("nickname").toString();
        UserProfileResDto findUserDto = userService.findUserByNickname(friendNickname);
        Long friendId = findUserDto.getId();

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Long userId = principalDetails.getUser().getId();

        friendshipService.deleteFriend(userId, friendId);

    }

    /**
     * 사용자의 nickname을 검색하여 찾기
     * @param nickname 쿼리 파라미터로 전달
     * @return 사용자 정보(UserProfileResDto -> nickname, profileImg, id), 친구 여부
     */
    @GetMapping("/friends/search")
    public ResponseEntity<SearchUserResDto> searchUser(@RequestParam("nickname") String nickname,
                                                       Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

        SearchUserResDto searchUserResDto = friendshipService
                .searchUser(nickname, principalDetails.getUser().getId());
        return ResponseEntity.ok().body(searchUserResDto);
    }

    /**
     * 친구 신청하기
     * @param toUserId 친구 신청 대상(상대방) id
     */
    @PostMapping("/friends/search")
    public void sendFriendRequest(Authentication authentication,
                                  @RequestParam("id") Long toUserId) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Long fromUserId = principalDetails.getUser().getId();
        //fromUserId -> 친구 신청한 사람 id, toUserId -> 친구 신청 상대방 id
        friendshipService.sendRequest(fromUserId, toUserId);
    }

    /**
     * 나에게 온 친구 요청(대기중 PENDING) 리스트
     * @return
     */
    @GetMapping("/friends/pending")
    public ResponseEntity<List<UserProfileResDto>> friendsRequestList(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        List<UserProfileResDto> result = friendshipService.findPendingFriendsToMe(principalDetails.getUser().getId());

        return ResponseEntity.ok().body(result);
    }

    /**
     * 친구 요청 수락 또는 거절
     * @param nickname 친구 요청한 사람 nickname
     * @param status 'reject' or 'accpet'
     */
    @PostMapping("/friends/pending")
    public void addFriend(Authentication authentication,
                          @RequestParam("nickname") String nickname,
                          @RequestParam("status") String status
                          ) {
        //toUserId -> 친구 요청을 받은 유저
        //fromUserId -> 친구 요청을 보낸 유저
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Long toUserId = principalDetails.getUser().getId();

        UserProfileResDto fromUserDto = userService.findUserByNickname(nickname);
        Long fromUserId = fromUserDto.getId();

        if ("accept".equals(status)) {
            friendshipService.addFriendAccept(toUserId, fromUserId);
        } else if ("reject".equals(status)) {
            friendshipService.addFriendReject(toUserId, fromUserId);
        }
    }
}
