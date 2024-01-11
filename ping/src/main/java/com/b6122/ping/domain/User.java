package com.b6122.ping.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true)
    private String nickname; // 사용자가 직접 입력하는 고유닉네임 지예님

    private String username;

    @Enumerated(EnumType.STRING)
    private UserRole role; // ROLE_USER or ROLE_ADMIN

    @OneToMany(mappedBy = "user")
    private List<Post> posts = new ArrayList<>();

    /** oauth2 연동 유저정보(username, providerId, provider) **/
    private String provider; //"google", "kakao", etc.
    private String providerId; //google, kakao 등 사용자의 고유Id (ex: google의 'sub'값 등)

    //회원 생성
//    public static User createUser() {
//
//    }
}
