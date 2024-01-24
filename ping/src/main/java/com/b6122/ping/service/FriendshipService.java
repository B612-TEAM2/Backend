package com.b6122.ping.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    @Transactional
    public void deleteFriend(String friendNickname, Long userId) {

    }
}
