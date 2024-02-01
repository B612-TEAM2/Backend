package com.b6122.ping.service;

import com.b6122.ping.domain.Friendship;
import com.b6122.ping.domain.User;
import com.b6122.ping.dto.FriendDto;
import com.b6122.ping.dto.UserProfileDto;
import com.b6122.ping.repository.datajpa.FriendshipDataRepository;
import com.b6122.ping.repository.datajpa.UserDataRepository;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipDataRepository friendshipDataRepository;
    private final UserService userService;
    private final UserDataRepository userDataRepository;

    /**
     * 친구의 고유 nickname으로 사용자의 친구 목록에서 삭제
     * @param friendId 전달 받은 삭제할 친구의 id
     * @param userId 요청한 사용자의 id
     */
    @Transactional
    public void deleteFriend(Long friendId, Long userId) {
        friendshipDataRepository.deleteFriendshipByIds(friendId, userId);
    }

    /**
     * 사용자의 id로 친구 목록 반환
     * @param id 요청한 사용자의 id
     * @return 친구 목록 (FriendDto 정보: nickname, profileImg)
     */
    public List<FriendDto> findFriendsById(Long id) {

        //fromUser, toUser 페치 조인해서 가져옴
        List<Friendship> friendshipList = friendshipDataRepository.findFriendshipsById(id);
        if (friendshipList.isEmpty()) {
            return Collections.emptyList();
        }

        List<FriendDto> friendDtos = new ArrayList<>();
        for (Friendship friendship : friendshipList) {
            User fromUser = friendship.getFromUser();
            User toUser = friendship.getToUser();
            byte[] imageBytes;
            FriendDto friendDto;

            //사용자가 친구 요청을 했을 경우 친구 상대방은 toUser
            if (fromUser.getId().equals(id)) {
                imageBytes = getByteArrayOfImageByPath(toUser.getProfileImagePath());
                friendDto = new FriendDto(id, imageBytes, toUser.getNickname());
                //사용자가 친구 요청을 받았을 경우 친구 상대방은 fromUser
            } else {
                imageBytes = getByteArrayOfImageByPath(fromUser.getProfileImagePath());
                friendDto = new FriendDto(fromUser.getId(), imageBytes, fromUser.getNickname());
            }
            friendDtos.add(friendDto);
        }
        return friendDtos;
    }

    /**
     * @param imagePath 서버의 이미지 저장 장소 경로
     * @return 이미지의 byte배열
     */
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


    /**
     * 친구 단건 조회(FriendShip 엔티티 가져오기, isFriend가 true인 경우만)
     */
    public Optional<Friendship> findFriendByIds(Long userId, Long friendId) {
        return friendshipDataRepository.findFriendshipByIds(userId, friendId);
    }

    /**
     * 친구 요청 보내기
     * @param userId (보내는 사람, fromUser)
     * @param friendId (받는 사람, toUser)
     */
    @Transactional
    public void sendRequest(Long userId, Long friendId) {

        User fromUser = userDataRepository.findById(userId).orElseThrow(RuntimeException::new);
        User toUser = userDataRepository.findById(friendId).orElseThrow(RuntimeException::new);

        Friendship friendship = Friendship.createFriendship(fromUser, toUser);

        Optional<Friendship> findFriendship = friendshipDataRepository.findPendingFriendShip(userId, friendId);
        if (findFriendship.isEmpty()) {
            friendshipDataRepository.save(friendship);
        }

    }
}
