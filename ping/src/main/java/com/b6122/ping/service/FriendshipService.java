package com.b6122.ping.service;

import com.b6122.ping.repository.datajpa.FriendshipDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipDataRepository friendshipDataRepository;

    @Transactional
    public void deleteFriend(String friendNickname, Long userId) {
        friendshipDataRepository.deleteFriendshipByFriendNicknameAndUserId(friendNickname, userId);
    }
}
