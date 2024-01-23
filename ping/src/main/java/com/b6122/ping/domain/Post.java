package com.b6122.ping.domain;

import com.b6122.ping.repository.PostRepository;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
@Entity
@Table(name = "post")
public class Post extends TimeEntity{

    private final PostRepository postRepository;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private  long id; //post id
    @Column(name = "title")
    private char title; // 제목

    @OneToMany(mappedBy = "post")
    private List<Like> likes = new ArrayList<>();

    @ColumnDefault("0")
    @Column(name = "viewCount")
    private int viewCount; // 조회수

    @ColumnDefault("0")
    @Column(name = "likeCount")
    private int likeCount; // 좋아요 수


    @Column(name = "content", nullable = false)
    private String content;

    public long getId(){
        return this.id;
    }

    public int getViewCount(){
        return this.viewCount;
    }

    public int getLikeCount(){return this.likeCount;};


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; //사용자

    @Enumerated(EnumType.STRING)
    private PostScope scope; //공개 범위 [private, friends, public]

    //연관관계 매서드//
    public void setUser(User user) {
        this.user = user;
        user.addPost(this); //user의 posts list에 post(this) 추가
    }


    //like 눌렀을때
    public void pushLIke(Long uid){
        postRepository.createLike(this.id,uid);
        postRepository.updateLikeCount(this.getLikeCount()+1, this.id);
    }



    //요청한 post를 반환하고 viewCount++
    public Post getPost(Long id) {
        Post post = postRepository.findById(id);
        postRepository.updateViewCount(post.getViewCount() + 1, post.getId());//중복 방지 구현 필요

        return post;
    }

}
