package com.b6122.ping.repository.datajpa;

import com.b6122.ping.domain.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendshipDataRepository extends JpaRepository<Friendship, Long> {

    @Query("select f from Friendship f " +
            "join fetch f.fromUser " +
            "join fetch f.toUser " +
            "where (f.fromUser.id = :userId or f.toUser.id = :userId) and f.isFriend = true")
    List<Friendship> findFriendshipsById(@Param("id") Long userId);
}
