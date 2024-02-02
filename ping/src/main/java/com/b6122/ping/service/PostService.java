package com.b6122.ping.service;

import com.b6122.ping.domain.Like;
import com.b6122.ping.domain.Post;
import com.b6122.ping.domain.PostScope;
import com.b6122.ping.domain.User;
import com.b6122.ping.dto.PostDto;
import com.b6122.ping.repository.LikeRepository;
import com.b6122.ping.repository.PostRepository;
import com.b6122.ping.repository.datajpa.LikeDataRepository;
import com.b6122.ping.repository.datajpa.PostDataRepository;
import com.b6122.ping.repository.datajpa.UserDataRepository;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    @Autowired
    private final PostRepository postRepository;

    @Autowired
    private final LikeRepository likeRepository;

    private final UserDataRepository userDataRepository;

    public Long createPost(PostDto postDto){
        Post post;
        post = new Post();
        post.setId(postDto.getId());
        User user = userDataRepository.findById(postDto.getUid()).orElseThrow(RuntimeException::new);
        post.setUser(user);
        post.setLocation(postDto.getLocation());
        post.setLatitude(postDto.getLatitude());
        post.setLongitude(postDto.getLongitude());
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setScope(postDto.getScope());
        post.setViewCount(postDto.getViewCount());
        post.setLikeCount(postDto.getLikeCount());
        post.setLikes(postDto.getLikes());

        return postRepository.save(post);
    }

    //Home-Map 클릭 전, 내가 작성한 모든 글의 pin띄우기
    public List<PostDto> getPinsHomeMap(long uid) {
        List<Post> posts = postRepository.findByUid(uid);
        return posts.stream().map(PostDto::pinHomeMap).collect(Collectors.toList());
    }


    //Home-Map 토글, pin 클릭시 해당 위치의 postList 반환
    public List<PostDto> getPostsHomeMap(float latitude, float longitude, long uid) {
        List<Post> posts = postRepository.findByLocationUser(latitude, longitude,uid);
        return posts.stream().map(PostDto::postPreviewHomeMap).collect(Collectors.toList());
    }

    //Home-List 토글, postList 반환
    public List<PostDto> getPostsHomeList(long uid) {
        List<Post> posts = postRepository.findByUid(uid);
        return posts.stream()
                .map(post-> PostDto.postPreviewHomeList(post, likeRepository))
                .collect(Collectors.toList());
    }
}
