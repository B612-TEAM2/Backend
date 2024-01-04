package com.b6122.ping.domain;

import com.fasterxml.jackson.databind.util.ArrayBuilders;
import jakarta.persistence.*;

@Entity
@Table(name = "post")
public class Post {
    @Id @GeneratedValue
    @Column(name = "post_id")
    private  long id; //post id

    private char title; // 제목

    private int likeNum; //좋아요 수

    private int viewerNum; // 조회수

    //content, date 추가 구현 필요


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; //사용자

    @Enumerated(EnumType.STRING)
    private PostAccess access; //공개 범위 [private, friend, public]

    //연관관계 매서드//
    public void setUser(User user) {
        this.user = user;
        user.getPosts().add(this); //user의 posts list에 post(this) 추가
    }

}
