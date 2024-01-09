package com.b6122.ping.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "post")
public class Post extends TimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private  long id; //post id
    @Column(name = "title")
    private char title; // 제목

    @OneToMany(mappedBy = "post")
    private List<Like> likes = new ArrayList<>();

    @ColumnDefault("0")
    @Column(name = "viewerNum")
    private int viewerNum; // 조회수


    @Column(name = "content", nullable = false)
    private String content;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; //사용자

    @Enumerated(EnumType.STRING)
    private PostScope scope; //공개 범위 [private, friends, public]

    //연관관계 매서드//
    public void setUser(User user) {
        this.user = user;
        user.getPosts().add(this); //user의 posts list에 post(this) 추가
    }

}
