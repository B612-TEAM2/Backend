package com.b6122.ping.service;

import com.b6122.ping.domain.User;
import com.b6122.ping.domain.UserRole;
import com.b6122.ping.dto.UserDto;
import com.b6122.ping.dto.UserProfileReqDto;
import com.b6122.ping.dto.UserProfileResDto;
import com.b6122.ping.oauth.provider.GoogleUser;
import com.b6122.ping.oauth.provider.KakaoUser;
import com.b6122.ping.oauth.provider.OAuthUser;
import com.b6122.ping.repository.datajpa.FriendshipDataRepository;
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
        return findUser.getProfileInfo();
    }
}
