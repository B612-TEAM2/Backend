package com.b6122.ping.controller;

import com.b6122.ping.auth.PrincipalDetails;
import com.b6122.ping.domain.User;
import com.b6122.ping.dto.CreateJwtRequestDto;
import com.b6122.ping.dto.UserDto;
import com.b6122.ping.service.JwtService;
import com.b6122.ping.service.UserService;
import com.b6122.ping.service.GoogleOAuthService;
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
    public ResponseEntity<Map<String, String>> createGoogleJwt(@RequestBody Map<String, Object> request) throws IOException {
        // Frontend sends the authorization code to the server
        String authorizationCode = request.get("code").toString();

        // Exchange the authorization code for an access token from Google
        String accessToken = GoogleOAuthService.getGoogleAccessToken(authorizationCode);

        // Use the access token to fetch user information from Google
        Map<String, Object> userInfo = GoogleOAuthService.getGoogleUserInfo(accessToken);

        // Process the user information and perform user registration if needed
        UserDto userDto = userService.joinOAuthUser(userInfo);

        // Return the JWT access token to the React server
        return ResponseEntity.ok().body(jwtService.createJwtAccessToken(userDto));
    }

}
