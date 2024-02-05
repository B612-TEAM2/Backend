package com.b6122.ping.service;

import com.b6122.ping.domain.Friendship;
import com.b6122.ping.domain.FriendshipRequestStatus;
import com.b6122.ping.domain.User;
import com.b6122.ping.dto.UserProfileResDto;
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
     * @param fromUserId 전달 받은 삭제할 친구의 id
     * @param toUserId 요청한 사용자의 id
     */
    @Transactional
    public void deleteFriend(Long fromUserId, Long toUserId) {
        Friendship findFriendship = friendshipDataRepository.findFriendshipByIds(fromUserId, toUserId).orElseThrow(RuntimeException::new);
        friendshipDataRepository.delete(findFriendship);
    }

    /**
     * 사용자의 id로 친구 목록 반환
     * @param id 요청한 사용자의 id
     * @return 친구 목록 (FriendDto 정보: nickname, profileImg, id)
     */
    public List<UserProfileResDto> findFriendsById(Long id) {

        //fromUser, toUser 페치 조인
        List<Friendship> friendshipList = friendshipDataRepository.findFriendshipsById(id);
        if (friendshipList.isEmpty()) {
            return Collections.emptyList();
        }

        List<UserProfileResDto> friendDtos = new ArrayList<>();
        for (Friendship friendship : friendshipList) {
            User fromUser = friendship.getFromUser();
            User toUser = friendship.getToUser();
            byte[] imageBytes;
            UserProfileResDto resDto;

            //사용자가 친구 요청을 했을 경우 친구 상대방은 toUser
            if (fromUser.getId().equals(id)) {
                imageBytes = getByteArrayOfImageByPath(toUser.getProfileImagePath());
                resDto = new UserProfileResDto(toUser.getId(), toUser.getNickname(), imageBytes);
                //사용자가 친구 요청을 받았을 경우 친구 상대방은 fromUser
            } else {
                imageBytes = getByteArrayOfImageByPath(fromUser.getProfileImagePath());
                resDto = new UserProfileResDto(fromUser.getId(), fromUser.getNickname(), imageBytes);
            }
            friendDtos.add(resDto);
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
    public Friendship findFriendByIds(Long toUserId, Long fromUserId) {

        return friendshipDataRepository.findFriendshipByIds(toUserId, fromUserId)
                .orElseThrow(EntityNotFoundException::new);
    }

    /**
     * 친구 요청 보내기
     * @param fromUserId ->친구 요청 보낸 사람 (사용자)
     * @param toUserId -> 친구 요청 받은 사람
     */
    @Transactional
    public void sendRequest(Long fromUserId, Long toUserId) {

        //중복 방지
        Optional<Friendship> findFriendShip = friendshipDataRepository.findFriendshipByIds(toUserId, fromUserId);
        if(findFriendShip.isEmpty()) {
            Optional<Friendship> findPendingFriendship = friendshipDataRepository.findPendingFriendShip(toUserId, fromUserId);
            if (findPendingFriendship.isEmpty()) {
                User fromUser = userDataRepository.findById(fromUserId).orElseThrow(EntityNotFoundException::new);
                User toUser = userDataRepository.findById(toUserId).orElseThrow(EntityNotFoundException::new);
                Friendship friendship = Friendship.createFriendship(fromUser, toUser);
                friendshipDataRepository.save(friendship);
            }
        }
    }

    /**
     * 친구 요청 수락
     * @param toUserId (친구 요청 받은 사람, 사용자 id)
     * @param fromUserId (친구 요청 보낸 사람)
     */
    @Transactional
    public void addFriendAccept(Long toUserId, Long fromUserId) {
        //toUser와 fromUser가 반대의 PENDING 상태 데이터 삭제
        deleteCounterPartPendingFriendship(toUserId, fromUserId);

        Friendship pendingFriendship = friendshipDataRepository.findPendingFriendShip(toUserId, fromUserId)
                .orElseThrow(RuntimeException::new);
        pendingFriendship.setRequestStatus(FriendshipRequestStatus.ACCEPTED);
        pendingFriendship.setIsFriend(true);
    }

    public void deleteCounterPartPendingFriendship(Long toUserIdArg, Long fromUserIdArg) throws RuntimeException {
        Long toUserId = fromUserIdArg;
        Long fromUserId = toUserIdArg;
        Optional<Friendship> pendingFriendShip = friendshipDataRepository.findPendingFriendShip(toUserId, fromUserId);
        pendingFriendShip.ifPresent(friendshipDataRepository::delete);
    }


    /**
     * 친구 요청 거절
     * @param toUserId (친구 요청 받은 사람)
     * @param fromUserId (친구 요청 보낸 사람)
     */
    @Transactional
    public void addFriendReject(Long toUserId, Long fromUserId) {
        Friendship friendship = friendshipDataRepository.findPendingFriendShip(toUserId, fromUserId).orElseThrow(RuntimeException::new);
        friendship.setRequestStatus(FriendshipRequestStatus.REJECTED);
    }

    /**
     * 나에게 친구 요청 상태인 유저정보 리스트 반환
     * @param toUserId (내 id)
     * @return
     */
    public List<UserProfileResDto> findPendingFriendsToMe(Long toUserId) {
        List<Friendship> pendingFriendShipsToMe = friendshipDataRepository.findPendingFriendShipsToMe(toUserId);
        List<UserProfileResDto> resDtos = new ArrayList<>();
        for (Friendship friendship : pendingFriendShipsToMe) {
            User fromUser = friendship.getFromUser();

            Long fromUserId = fromUser.getId();
            byte[] profileImg = userService.getByteArrayOfImageByPath(fromUser.getProfileImagePath());
            String nickname = fromUser.getNickname();

            UserProfileResDto dto = new UserProfileResDto(fromUserId, nickname, profileImg);
            resDtos.add(dto);
        }
        return resDtos;
    }
}
