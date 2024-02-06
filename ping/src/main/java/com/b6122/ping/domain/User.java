package com.b6122.ping.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true)
    private String nickname; // 사용자가 직접 입력하는 고유닉네임

    /** oauth2 연동 유저정보(username, providerId, provider) **/
    private String provider; //"google", "kakao", etc.
    private String providerId; //google, kakao 등 사용자의 고유Id (ex: google의 'sub'값 등)
    private String username; // provider + _ + providerId

    private String profileImagePath;

    @Enumerated(EnumType.STRING)
    private UserRole role; // ROLE_USER or ROLE_ADMIN

    @OneToMany(mappedBy = "user")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "fromUser", cascade = CascadeType.REMOVE)
    private List<Friendship> sentFriendshipRequests = new ArrayList<>();

    @OneToMany(mappedBy = "toUser", cascade = CascadeType.REMOVE)
    private List<Friendship> receivedFriendshipRequests = new ArrayList<>();

    public void addPost(Post p) {//외부에서 post 생성시 posts list에 추가
        this.posts.add(p);
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setProfileImagePath(String path) {
        this.profileImagePath = path;
    }
}
