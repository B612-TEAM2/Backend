package com.b6122.ping.domain;

import com.b6122.ping.repository.PostRepository;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import java.util.ArrayList;
import java.util.List;

//위치 정보 추가
//생성날짜 추가
//이미지 파일 가져오기
//friend 글 목록 페이지: 이미지 , 제몯, 냉ㅇ 15자, 공개범위, 날짜, 좋앙, 요청 id가 좋앙 눌러는지, 최신순
//탈퇴 시 글 삭제
//friend map: 받은 위치정보에 해당하는 글을 찾아 같은 주소로 이미지, 냉ㅇ,공개 범위, 날짜, 제목
//public : 공개범위가 public인 글만
//위치 string
@RequiredArgsConstructor
@Entity
@Getter @Setter
@Table(name = "post")
@NoArgsConstructor
public class Post extends TimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id ; //post id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; //사용자

    @Column
    private String location; //위치

    @Column
    private float  latitude; //위도

    @Column
    private float longitude; //경도

    @Column(name = "title")
    private String title; // 제목
    @Column(name = "content", nullable = false)
    private String content;
    @Enumerated(EnumType.STRING)
    private PostScope scope; //공개 범위 [private, friends, public]
    @ColumnDefault("0")
    @Column(name = "viewCount")
    private int viewCount; // 조회수
    @ColumnDefault("0")
    @Column(name = "likeCount")
    private int likeCount; // 좋아요 수
    @OneToMany(mappedBy = "post")
    private List<Like> likes = new ArrayList<>();

    //연관관계 매서드//
    public void setUser(User user) {
        user.addPost(this); //user의 posts list에 post(this) 추가
    }

}
