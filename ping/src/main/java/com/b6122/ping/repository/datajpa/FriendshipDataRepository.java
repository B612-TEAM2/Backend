package com.b6122.ping.repository.datajpa;

import com.b6122.ping.domain.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipDataRepository extends JpaRepository<Friendship, Long> {

    //친구 목록 조회
    @Query("select f from Friendship f" +
            " join fetch f.fromUser" +
            " join fetch f.toUser" +
            " where (f.fromUser.id = :userId or f.toUser.id = :userId) and f.isFriend = true")
    List<Friendship> findFriendshipsById(@Param("userId") Long userId);

    //친구 단건 삭제
    @Query("delete from Friendship f" +
            " where f.isFriend = true and" +
            " ((f.fromUser.id =:userId and f.toUser.nickname =:friendId)" +
            " or (f.toUser.id =:userId and f.fromUser.nickname =:friendId))")
    void deleteFriendshipByIds(@Param("friendId") Long friendId,
                                                   @Param("userId") Long userId);

    //친구 단건 조회
    @Query("select f from Friendship f" +
            " where f.isFriend = true" +
            " and ((f.toUser.id =:friendId and f.fromUser.id = :userId)" +
            " or (f.toUser.id = :userId and f.fromUser.id = :friendId))")
    Optional<Friendship> findFriendshipByIds(@Param("friendId") Long friendId,
                                        @Param("userId") Long userId);
}

