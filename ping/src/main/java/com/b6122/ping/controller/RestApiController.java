package com.b6122.ping.controller;

import com.b6122.ping.auth.PrincipalDetails;
import com.b6122.ping.domain.User;
import com.b6122.ping.dto.CreateJwtRequestDto;
import com.b6122.ping.dto.UserDto;
import com.b6122.ping.service.JwtService;
import com.b6122.ping.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequiredArgsConstructor
public class RestApiController {

    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/oauth/jwt/google")
    public ResponseEntity<Map<String, String>> createJwt(@RequestBody CreateJwtRequestDto jwtRequestDto) {
        //회원가입
        UserDto userDto = userService.joinOAuthUser(jwtRequestDto);

        //jwt accessToken 발급
        return ResponseEntity.ok().body(jwtService.createJwtAccessToken(userDto));
    }
}
