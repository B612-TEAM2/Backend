package com.b6122.ping.service;

import com.b6122.ping.domain.Friendship;
import com.b6122.ping.domain.User;
import com.b6122.ping.dto.FriendDto;
import com.b6122.ping.repository.datajpa.FriendshipDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipDataRepository friendshipDataRepository;

    @Transactional
    public void deleteFriend(String friendNickname, Long userId) {
        friendshipDataRepository.deleteFriendshipByFriendNicknameAndUserId(friendNickname, userId);
    }

    //친구 목록 불러오기
    public List<FriendDto> findFriendsById(Long id) {

        //fromUser, toUser 페치 조인
        List<Friendship> friendshipList = friendshipDataRepository.findFriendshipsById(id);
        if (friendshipList.isEmpty()) {
            return Collections.emptyList();
        }

        List<FriendDto> friendDtos = new ArrayList<>();
        for (Friendship friendship : friendshipList) {
            User fromUser = friendship.getFromUser();
            User toUser = friendship.getToUser();

            //사용자가 친구 요청을 했을 경우 친구 상대방은 toUser
            if (fromUser.getId().equals(id)) {
                byte[] imageBytes = getByteArrayOfImageByPath(toUser.getProfileImagePath());
                friendDtos.add(new FriendDto(imageBytes, toUser.getNickname()));
                //사용자가 친구 요청을 받았을 경우 친구 상대방은 fromUser
            } else {
                byte[] imageBytes = getByteArrayOfImageByPath(fromUser.getProfileImagePath());
                friendDtos.add(new FriendDto(imageBytes, fromUser.getNickname()));
            }
        }
        return friendDtos;
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
