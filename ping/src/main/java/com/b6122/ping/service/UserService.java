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

    public List<UserDto> findFriends(Long id) {

        List<Friendship> friendshipList = friendshipDataRepository.findFriendshipsById(id);
        if (friendshipList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> friendsId = new ArrayList<>();
        for (Friendship friendship : friendshipList) {
            // Friendship의 fromUser와 toUser 중 현재 사용자의 ID와 다른 사용자의 ID를 찾아서 friendsId에 추가
            if (friendship.getFromUser().getId().equals(id)) {
                friendsId.add(friendship.getToUser().getId());
            } else {
                friendsId.add(friendship.getFromUser().getId());
            }
        }

        // friendsId를 이용하여 사용자 정보를 불러오기
        List<User> friends = userDataRepository.findAllById(friendsId);

        // User 정보를 UserDto로 변환 후(다른 dto를 따로 만들어야할듯, 일단 userdto로) 리스트에 추가
        List<UserDto> friendDtos = friends.stream()
                .map(user -> new UserDto(user.getId(), user.getProvider(), user.getProviderId(), user.getRole()))
                .collect(Collectors.toList());

        return friendDtos;
    }

    @Transactional
    public void deleteAccount(Long id) {
        userDataRepository.deleteById(id);
    }
}
