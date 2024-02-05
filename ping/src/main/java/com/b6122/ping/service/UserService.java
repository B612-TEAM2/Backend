package com.b6122.ping.service;

import com.b6122.ping.domain.User;
import com.b6122.ping.domain.UserRole;
import com.b6122.ping.dto.UserDto;
import com.b6122.ping.dto.UserProfileReqDto;
import com.b6122.ping.dto.UserProfileResDto;
import com.b6122.ping.oauth.provider.GoogleUser;
import com.b6122.ping.oauth.provider.KakaoUser;
import com.b6122.ping.oauth.provider.OAuthUser;
import com.b6122.ping.repository.datajpa.UserDataRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDataRepository userDataRepository;

    //서버가 받은 이미지가 저장되는 로컬 디스크 주소
    @Value("${profile.image.upload-path}")
    private String profileImagePath;

    /**
     * 리소스 서버(kakao, google)로 부터 사용자 정보를 받은 후 그것을 바탕으로 회원가입
     * @param userInfoMap 사용자 정보 Map
     * @return UserDto (id, username)
     * @throws IOException
     */
    @Transactional
    public UserDto joinOAuthUser(Map<String, Object> userInfoMap) throws IOException {

        //OAuthUser 생성을 위한 매핑
        String provider = userInfoMap.get("provider").toString();
        String providerId = userInfoMap.get("id").toString();
        String username = provider + "_" + providerId;

        Map<String, Object> userInfo = new HashMap<>();

        userInfo.put("username", username);
        userInfo.put("provider", provider);
        userInfo.put("providerId", providerId);

        //OAuthUser 생성 -> 나중에 프로바이더마다 다른 회원가입 정책을 할 수도 있기 때문에 추상화
        OAuthUser oAuthUser = createOAuthUser(provider, userInfo);

        //db에 회원 등록이 되어있는지 확인후, 안되어 있다면 회원가입 시도
        User findUser = userDataRepository
                .findByUsername(oAuthUser.getName())
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
        return new UserDto(findUser.getId(), findUser.getUsername());

    }

    //OAuthUser 생성 메소드. 리소스 서버에 따라 분기.
    protected OAuthUser createOAuthUser(String provider, Map<String, Object> userInfo) {
        switch (provider) {
            case "google":
                return new GoogleUser(userInfo);
            case "kakao":
                return new KakaoUser(userInfo);
            default:
                return null;
        }
    }

    @Transactional
    public void updateProfile(UserProfileReqDto reqDto) {
        User user = userDataRepository.findById(reqDto.getId()).orElseThrow(RuntimeException::new);
        user.updateProfile(reqDto);
    }

    //계정 삭제
    @Transactional
    public void deleteAccount(Long id) {
        userDataRepository.deleteById(id);
    }

    /**
     * 사용자 정보(이미지, 닉네임) 가져오기
     * @param id 사용자의 id
     * @return 사용자 정보(UserProfileResDto 정보: nickname, profileImg, id)
     */
    public UserProfileResDto getUserProfile(Long id) {
        User user = userDataRepository.findById(id).orElseThrow(RuntimeException::new);
        return user.getProfileInfo();
    }

    /**
     * nickname으로 유저 검색
     * @param nickname
     * @return UserProfileResDto(nickname, profileImg, id)
     */
    public UserProfileResDto findUserByNickname(String nickname) {
        User findUser = userDataRepository.findByNickname(nickname).orElseThrow(EntityNotFoundException::new);
        byte[] imageBytes = findUser.getByteArrayOfProfileImgByPath();
        return new UserProfileResDto(findUser.getNickname(), imageBytes, findUser.getId());
    }
}
