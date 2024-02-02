package com.b6122.ping.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id")
    private User fromUser; //친구 요청을 보낸 User

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id")
    private User toUser; //친구 요청을 받은 User

    @Enumerated(EnumType.STRING)
    private FriendshipRequestStatus requestStatus; // PENDING, ACCEPTED, REJECTED

    private boolean isFriend = false;

    public void setIsFriend(boolean isFriend) {
        this.isFriend = isFriend;
    }

    public void setRequestStatus(FriendshipRequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }
    //친구 요청 시 메소드
    public static Friendship createFriendship(User fromUser, User toUser) {
        Friendship friendship = new Friendship();
        friendship.fromUser = fromUser;
        friendship.toUser = toUser;
        friendship.requestStatus = FriendshipRequestStatus.PENDING;
        return friendship;
    }
}

