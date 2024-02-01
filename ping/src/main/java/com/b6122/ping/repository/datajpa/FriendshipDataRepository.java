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
            " ((f.fromUser.id =:userId and f.toUser.id =:friendId)" +
            " or (f.toUser.id =:userId and f.fromUser.id =:friendId))")
    void deleteFriendshipByIds(@Param("friendId") Long friendId,
                                                   @Param("userId") Long userId);

    //친구 단건 조회
    @Query("select f from Friendship f" +
            " where f.isFriend = true" +
            " and ((f.toUser.id =:friendId and f.fromUser.id = :userId)" +
            " or (f.toUser.id = :userId and f.fromUser.id = :friendId))")
    Optional<Friendship> findFriendshipByIds(@Param("friendId") Long friendId,
                                        @Param("userId") Long userId);

    /**
     * fromUser가 보낸 아직 대기 중인(PENDING) 친구 요청
     * @param toUserId 친구 요청 받은 사람 id
     * @param fromUserId 친구 요청 보낸 사람 id
     * @return
     */
    @Query("select f from Friendship f" +
            " where f.isFriend = false" +
            " and f.requestStatus = com.b6122.ping.domain.FriendshipRequestStatus.PENDING" +
            " and f.toUser.id = :toUserId and f.fromUser.id = :fromUserId ")
    Optional<Friendship> findPendingFriendShip(@Param("toUserId") Long toUserId,
            @Param("fromUserId") Long fromUserId);


}

