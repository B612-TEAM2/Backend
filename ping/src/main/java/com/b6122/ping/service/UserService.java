package com.b6122.ping.service;

import com.b6122.ping.domain.Friendship;
import com.b6122.ping.domain.User;
import com.b6122.ping.domain.UserRole;
import com.b6122.ping.dto.UserDto;
import com.b6122.ping.oauth.provider.GoogleUser;
import com.b6122.ping.oauth.provider.KakaoUser;
import com.b6122.ping.oauth.provider.NaverUser;
import com.b6122.ping.oauth.provider.OAuthUser;
import com.b6122.ping.repository.datajpa.FriendshipDataRepository;
import com.b6122.ping.repository.datajpa.UserDataRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDataRepository userDataRepository;
    private final FriendshipDataRepository friendshipDataRepository;

    @Transactional
    public UserDto joinOAuthUser(Map<String, Object> userInfoMap) throws IOException {
        
        //OAuthUser 생성을 위한 매핑
        String provider = userInfoMap.get("provider").toString();
        String providerId = userInfoMap.get("id").toString();
        String username = provider + "_" + providerId;

        userInfoMap = new HashMap<>();

        userInfoMap.put("provider", provider);
        userInfoMap.put("providerId", providerId);
        userInfoMap.put("username", username);

        //OAuthUser 생성 -> 나중에 프로바이더마다 다른 회원가입 정책을 할 수도 있기 때문에 추상화
        OAuthUser oAuthUser = createOAuthUser(provider, userInfoMap);

        //db에 회원 등록이 되어있는지 확인후, 안되어 있다면 회원가입 시도
        User findUser = userDataRepository
                .findByUsername(oAuthUser.getProvider() + "_" + oAuthUser.getProviderId())
                .orElseGet(() -> {
                    User user = User.builder()
                            .provider(oAuthUser.getProvider())
                            .providerId(oAuthUser.getProviderId())
                            .username(oAuthUser.getName())
                            .role(UserRole.ROLE_USER)
                            .build();

                    // 회원가입
                    return userDataRepository.save(user);
                });
        return new UserDto( findUser.getId(), findUser.getProvider(),
                findUser.getProviderId(), findUser.getRole());

    }

    //OAuthUser 생성 메소드. 리소스 서버에 따라 분기.
    protected OAuthUser createOAuthUser(String provider, Map<String, Object> userInfo) {
        switch (provider) {
            case "google":
                return new GoogleUser(userInfo);
            case "kakao":
                return new KakaoUser(userInfo);
            case "naver":
                return new NaverUser(userInfo);
            default:
                return null;
        }
    }

    //친구 목록 불러오기
    public List<UserDto> findFriends(Long id) {

        //fromUser, toUser 페치 조인
        List<Friendship> friendshipList = friendshipDataRepository.findFriendshipsById(id);
        if (friendshipList.isEmpty()) {
            return Collections.emptyList();
        }

        List<UserDto> friendDtos = new ArrayList<>();
        for (Friendship friendship : friendshipList) {
            User fromUser = friendship.getFromUser();
            User toUser = friendship.getToUser();

            //사용자가 친구 요청을 했을 경우 친구 상대방은 toUser
            if (fromUser.getId().equals(id)) {
                friendDtos.add(new UserDto(toUser.getId(), toUser.getProvider(),
                        toUser.getProviderId(), toUser.getRole()));

            //사용자가 친구 요청을 받았을 경우 친구 상대방은 fromUser
            } else {
                friendDtos.add(new UserDto(fromUser.getId(), fromUser.getProvider(),
                        fromUser.getProviderId(), fromUser.getRole()));
            }
        }
        return friendDtos;
    }

    //계정 삭제
    @Transactional
    public void deleteAccount(Long id) {
        userDataRepository.deleteById(id);
    }
}
