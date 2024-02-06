package com.b6122.ping.dto;

import com.b6122.ping.domain.Like;
import com.b6122.ping.domain.Post;
import com.b6122.ping.domain.PostScope;
import com.b6122.ping.domain.User;
import com.b6122.ping.repository.LikeRepository;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter @Setter
@RequiredArgsConstructor
public class PostDto {
    private  Long id; //post id

    private Long uid; //사용자

    private String location; //위치

    private float  latitude; //위도

    private float longitude; //경도

    private String title; // 제목

    private String content;

    private PostScope scope; //공개 범위 [private, friends, public]

    private int viewCount; // 조회수

    private int likeCount; // 좋아요 수

    private boolean myLike; //본인이 글에 좋아요 눌렀는지

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    private String contentPreview;

    @OneToMany(mappedBy = "post")
    private List<Like> likes = new ArrayList<>();


    //pin- 위도, 경도,postId
    //Home-Map, 모든 글의 pin 보여주기
    public static PostDto pinHomeMap(Post post) {
        PostDto postDto = new PostDto();
        postDto.setId(post.getUser().getId());
        postDto.setLongitude(post.getLongitude());
        postDto.setLatitude(post.getLatitude());
        return postDto;
    }

    //Home-Map 토글, pin클릭시 postPreview보여주기
    public static PostDto postPreviewHomeMap(Post post) {
        PostDto postDto = new PostDto();
        postDto.setId(post.getId());
        postDto.setTitle(post.getTitle());
        //postDto.setImageUrl(post.getImageUrl());  // Adjust based on your entity fields
        postDto.setScope(post.getScope());
        postDto.setCreatedDate(post.getCreatedDate());
        postDto.setContentPreview(truncateContent(post.getContent(), 15)); // Adjust for content preview
        return postDto;
    }


    //Home-List 토글
    public static PostDto postPreviewHomeList(Post post, LikeRepository likeRepository) {
        PostDto postDto = new PostDto();
        postDto.setId(post.getId());
        postDto.setTitle(post.getTitle());
        //postDto.setImageUrl(post.getImageUrl());  // Adjust based on your entity fields
        postDto.setScope(post.getScope());
        postDto.setLikeCount(post.getLikeCount());
        postDto.setMyLike(likeRepository.checkMyLike(post.getId(), post.getUser().getId()));//사용자가 post에 좋아요 눌렀다면 myLike == True
        postDto.setCreatedDate(post.getCreatedDate());
        postDto.setContentPreview(truncateContent(post.getContent(), 15)); // Adjust for content preview
        return postDto;
    }
    //Friends-Map 토글

    //Friends-List 토글

    //글 보기 페이지

    //글 작성 페이지-정보 저장

    private static String truncateContent(String content, int maxLength) {
        if (content.length() <= maxLength) {
            return content;
        } else {
            return content.substring(0, maxLength) + "...";
        }
    }
}