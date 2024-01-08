package com.b6122.ping.service;

import com.b6122.ping.domain.User;
import com.b6122.ping.domain.UserRole;
import com.b6122.ping.dto.CreateJwtRequestDto;
import com.b6122.ping.dto.UserDto;
import com.b6122.ping.oauth.provider.GoogleUser;
import com.b6122.ping.oauth.provider.KakaoUser;
import com.b6122.ping.oauth.provider.NaverUser;
import com.b6122.ping.oauth.provider.OAuthUser;
import com.b6122.ping.repository.datajpa.UserDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDataRepository userDataRepository;

    @Transactional
    public void join(User user) {
        userDataRepository.save(user);
    }

    @Transactional
    public UserDto joinOAuthUser(CreateJwtRequestDto jwtRequestDto) {

        //OAuthUser 생성을 위한 매핑
        String provider = jwtRequestDto.getProvider();
        String providerId = jwtRequestDto.getProviderId();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("providerId", providerId);
        userInfo.put("provider", provider);

        //OAuthUser 생성 -> 나중에 프로바이더마다 다른 회원가입 정책을 할 수도 있기 때문에 추상화
        OAuthUser oAuthUser = createOAuthUser(provider, userInfo);

        //db에 회원 등록이 되어있는지 확인후, 안되어 있다면 회원가입 시도
        User findUser = userDataRepository
                .findByUsername(oAuthUser.getProvider() + "_" + oAuthUser.getProviderId())
                .orElseGet(() -> {
                    User user = User.builder()
                            .username(oAuthUser.getProvider() + "_" + oAuthUser.getProviderId())
                            .provider(oAuthUser.getProvider())
                            .providerId(oAuthUser.getProviderId())
                            .role(UserRole.ROLE_USER)
                            .build();

                    // 회원가입
                    return userDataRepository.save(user);
                });
        return new UserDto(findUser.getId(), findUser.getProvider(),
                findUser.getProviderId(), findUser.getUsername(), findUser.getRole());

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

}
