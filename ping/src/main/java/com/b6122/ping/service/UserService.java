package com.b6122.ping.service;

import com.b6122.ping.domain.Friendship;
import com.b6122.ping.domain.User;
import com.b6122.ping.domain.UserRole;
import com.b6122.ping.dto.FriendDto;
import com.b6122.ping.dto.UserDto;
import com.b6122.ping.dto.UserInfoDto;
import com.b6122.ping.oauth.provider.GoogleUser;
import com.b6122.ping.oauth.provider.KakaoUser;
import com.b6122.ping.oauth.provider.NaverUser;
import com.b6122.ping.oauth.provider.OAuthUser;
import com.b6122.ping.repository.datajpa.FriendshipDataRepository;
import com.b6122.ping.repository.datajpa.UserDataRepository;
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
import java.nio.file.Path;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDataRepository userDataRepository;
    private final FriendshipDataRepository friendshipDataRepository;

    @Value("${profile.image.upload-path}")
    private String profileImagePath;

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
        return new UserDto(findUser.getId(), findUser.getProvider(),
                findUser.getProviderId(), findUser.getUsername());

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

    @Transactional
    public void updateProfile(MultipartFile file, String nickname, Long userId) {
        try {
            User user = userDataRepository.findById(userId).orElseThrow(RuntimeException::new);
            user.setNickname(nickname);
            user.setProfileImagePath(saveProfileImage(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String saveProfileImage(MultipartFile file) throws IOException {
        String imageName = UUID.randomUUID() + file.getOriginalFilename();
        String path = profileImagePath;
        File fileDir = new File(profileImagePath);

        //지정한 디렉토리가 없으면 생성
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }

        //새로운 파일로 변환해서 지정한 경로에 저장.
        file.transferTo(new File(path, imageName));

        //이미지 찾아올 때 모든 경로가 필요하기 때문에 아래와 같이 저장.
        path = profileImagePath + "\\" + imageName;

        return path;
    }

    //계정 삭제
    @Transactional
    public void deleteAccount(Long id) {
        userDataRepository.deleteById(id);
    }

    public UserInfoDto userWithNicknameAndImage(Long id) {
        User user = userDataRepository.findById(id).orElseThrow(RuntimeException::new);
        String nickname = user.getNickname();
        byte[] imageBytes = getByteArrayOfImageByPath(user.getProfileImagePath());
        return new UserInfoDto(nickname, imageBytes);
    }

    public byte[] getByteArrayOfImageByPath(String imagePath) {
        try {
            Resource resource = new UrlResource(Path.of(imagePath).toUri());
            if (resource.exists() && resource.isReadable()) {
                // InputStream을 사용하여 byte 배열로 변환
                try (InputStream inputStream = resource.getInputStream()) {
                    byte[] data = new byte[inputStream.available()];
                    inputStream.read(data);
                    return data;
                }
            } else {
                // 이미지를 찾을 수 없는 경우 예외 또는 다른 처리 방법을 선택
                throw new RuntimeException("Image not found");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
