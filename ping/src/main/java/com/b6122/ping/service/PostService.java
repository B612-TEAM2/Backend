package com.b6122.ping.service;

import com.b6122.ping.domain.Like;
import com.b6122.ping.domain.Post;
import com.b6122.ping.domain.PostScope;
import com.b6122.ping.domain.User;
import com.b6122.ping.dto.PostDto;
import com.b6122.ping.repository.LikeRepository;
import com.b6122.ping.repository.PostRepository;
import com.b6122.ping.repository.datajpa.UserDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    @Autowired
    private final PostRepository postRepository;

    @Autowired
    private final LikeRepository likeRepository;

    private final UserDataRepository userDataRepository;

    @Transactional
    public Long createPost(PostDto postDto){
        Post post;
        post = new Post();
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
        post.saveImagesInStorage(postDto.getImgs()); //이미지 저장 MultiPartfile->path
        return postRepository.save(post);
    }

//post 수정
    public Long modifyPost(PostDto postDto){
        Post post;
        post = new Post();
        post.setId(postDto.getId());
        post.setLocation(postDto.getLocation());
        post.setLatitude(postDto.getLatitude());
        post.setLongitude(postDto.getLongitude());
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setScope(postDto.getScope());
        post.saveImagesInStorage(postDto.getImgs()); //이미지 저장 MultiPartfile->path
        return postRepository.updatePost(post);
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
    public List<PostDto> getPostsHomeList(Long uid) {
        List<Post> posts = postRepository.findByUid(uid);
        return posts.stream()
                .map(post-> PostDto.postPreviewHomeList(post, likeRepository))
                .collect(Collectors.toList());
    }

    //글 전체보기 요청
    public PostDto getPostInfo(Long pid, Long uid) {
        Post post = postRepository.findById(pid);

        if(post.getUser().getId()!=uid) {//사용자와 글 작성자와 다른 경우만 viewCount++
            postRepository.updateViewCount(post.getViewCount() + 1, post.getId());
        }

        return PostDto.postInfo(post, likeRepository);
    }


    public void toggleLike(long postId, long userId) {
        Optional<Like> existingLike = likeRepository.findByPostIdAndUserId(postId, userId);

        if (existingLike.isPresent()) {
            // If like exists, delete it
            likeRepository.delete(existingLike.get().getId());
        }

        else {
            // If like does not exist, create it
            Like newLike = new Like();
//            newLike.getPost().setId(postId);
//            newLike.getUser().setId(userId);
            newLike.setPost(postRepository.findById(postId));
            newLike.setUser(userDataRepository.findById(userId).orElseThrow(RuntimeException::new));
            likeRepository.save(newLike);
        }
    }


}