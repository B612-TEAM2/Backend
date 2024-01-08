package com.b6122.ping.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.b6122.ping.config.jwt.JwtProperties;
import com.b6122.ping.domain.User;
import com.b6122.ping.domain.UserRole;
import com.b6122.ping.dto.CreateJwtRequestDto;
import com.b6122.ping.oauth.provider.OAuthUser;
import com.b6122.ping.repository.datajpa.UserDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final UserDataRepository userDataRepository;
    private final UserService userService;

    public Map<String, String> createJwtAccessToken(CreateJwtRequestDto jwtRequestDto) {
        String provider = jwtRequestDto.getProvider();
        String providerId = jwtRequestDto.getProviderId();
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("providerId", providerId);
        userInfo.put("provider", provider);

        //OAuthUser 생성 -> 나중에 프로바이더마다 다른 회원가입 정책을 할 수도 있기 때문에 추상화
        OAuthUser oAuthUser = userService.createOAuthUser(provider, userInfo);

        //이미 가입되어 있는지 확인 후, Null 반환하면 회원가입 시도
        User userEntity = userDataRepository
                .findByUsername(oAuthUser.getProvider() + "_" + oAuthUser.getProviderId())
                .orElseGet(() -> {
                    User user = User.builder()
                            .username(oAuthUser.getProvider() + "_" + oAuthUser.getProviderId())
                            .provider(oAuthUser.getProvider())
                            .providerId(oAuthUser.getProviderId())
                            .role(UserRole.ROLE_USER)
                            .build();

                    //회원가입
                    userService.join(user);
                    return user;
                });

        //accessToken 생성
        String jwtToken = JWT.create()
                .withSubject(userEntity.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME))
                .withClaim("id", userEntity.getId())
                .withClaim("username", userEntity.getUsername())
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));

        //responseBody에 값 저장해서 return
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("access_token", jwtToken);
        responseBody.put("token_prefix", JwtProperties.TOKEN_PREFIX);
        responseBody.put("expires_in", String.valueOf(JwtProperties.EXPIRATION_TIME));

        return responseBody;
    }
}
