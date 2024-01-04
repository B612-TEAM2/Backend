package com.b6122.ping.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
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

    private boolean isFriend;

    //친구 요청 시 메소드
    //fromUser와 toUser사이 정방향/역방향 레코드 모두 추가
    public static Friendship createFriendship() {

    }
}

