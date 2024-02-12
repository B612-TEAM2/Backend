package com.b6122.ping.service;

import com.b6122.ping.domain.Friendship;
import com.b6122.ping.domain.FriendshipRequestStatus;
import com.b6122.ping.domain.User;
import com.b6122.ping.dto.SearchUserResDto;
import com.b6122.ping.dto.UserProfileResDto;
import com.b6122.ping.repository.datajpa.FriendshipDataRepository;
import com.b6122.ping.repository.datajpa.UserDataRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * 사용자의 친구 목록에서 삭제
     */
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        Optional<Friendship> findFriendship = findFriendByIds(userId, friendId);
        if(findFriendship.isPresent()) {
            friendshipDataRepository.delete(findFriendship.get());
        }
    }

    /**
     * 사용자의 id로 친구 목록 반환
     * @param userId 요청한 사용자의 id
     * @return 친구 목록 (FriendDto 정보: nickname, profileImg, id)
     */
    public List<UserProfileResDto> getFriendsProfile(Long userId) {

        //fromUser, toUser 페치 조인
        List<Friendship> friendshipList = friendshipDataRepository.findFriendshipsById(userId);
        if (friendshipList.isEmpty()) {
            return Collections.emptyList();
        }

        List<UserProfileResDto> userProfiles = new ArrayList<>();
        for (Friendship friendship : friendshipList) {
            UserProfileResDto userProfile = friendship.getUserProfile(userId);
            userProfiles.add(userProfile);
        }
        return userProfiles;
    }



    /**
     * 친구 단건 조회(FriendShip 엔티티 가져오기, isFriend가 true인 경우만)
     */
    public Optional<Friendship> findFriendByIds(Long userId, Long friendId) {
        return friendshipDataRepository.findFriendshipByIds(userId, friendId);
    }

    /**
     * 친구 요청 보내기
     * @param fromUserId ->친구 요청 보낸 사람 (사용자)
     * @param toUserId -> 친구 요청 받은 사람
     */
    @Transactional
    public void sendRequest(Long fromUserId, Long toUserId) {

        //중복 방지
        Optional<Friendship> findFriendShip = findFriendByIds(fromUserId, toUserId);
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
        List<Friendship> friendshipList = friendshipDataRepository.findPendingFriendShipsToMe(toUserId);

        if (friendshipList.isEmpty()) {
            return Collections.emptyList();
        }

        List<UserProfileResDto> userProfiles = new ArrayList<>();
        for (Friendship friendship : friendshipList) {
            UserProfileResDto userProfile = friendship.getUserProfile(toUserId);
            userProfiles.add(userProfile);
        }
        return userProfiles;
    }

    public SearchUserResDto searchUser(String nickname, Long userId) {
        Long friendId = userService.findUserByNickname(nickname);
        User friendEntity = userDataRepository.findById(friendId).orElseThrow(RuntimeException::new);
        Optional<Friendship> findFriendship = findFriendByIds(userId, friendId);
        boolean isFriend = findFriendship.isPresent();

        return new SearchUserResDto(nickname, friendEntity.getByteArrayOfProfileImgByPath(), isFriend);
    }
}
