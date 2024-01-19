package com.b6122.ping.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.b6122.ping.config.jwt.JwtProperties;
import com.b6122.ping.domain.User;
import com.b6122.ping.domain.UserRole;
import com.b6122.ping.dto.CreateJwtRequestDto;
import com.b6122.ping.dto.UserDto;
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

    public Map<String, String> createJwtAccessToken(UserDto userDto) {

        //accessToken 생성
        String jwtToken = JWT.create()
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME))
                .withClaim("id", userDto.getId())
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));

        //responseBody에 값 저장해서 return
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("access_token", jwtToken);
        responseBody.put("token_prefix", JwtProperties.TOKEN_PREFIX);
        responseBody.put("expires_in", String.valueOf(JwtProperties.EXPIRATION_TIME));

        return responseBody;
    }
}
