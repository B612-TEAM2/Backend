package com.b6122.ping.controller;

import com.b6122.ping.auth.PrincipalDetails;
import com.b6122.ping.dto.PostDto;
import com.b6122.ping.service.PostService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Getter@Setter
@RestController
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    //글 작성 후 디비 저장
    @PostMapping("/posts/home/store")
    public ResponseEntity getPost(@RequestBody @Validated PostDto postDto){
        Long pid = postService.createPost(postDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pid);
    }

    //글 수정 후 디비 저장
    @PostMapping("/posts/home/edit")
    public ResponseEntity modifyPost(@RequestBody @Validated PostDto postDto){
        Long pid = postService.modifyPost(postDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pid);
    }

    //Home-Map, 내 모든 글의 pin 반환
    @GetMapping("/posts/home/pins")
    public ResponseEntity<List<PostDto>> showPinsHome(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Long uid = principalDetails.getUser().getId();
        List<PostDto> posts = postService.getPinsHomeMap(uid);
        return ResponseEntity.ok(posts);
    }

    //Home-Map 클릭, postList 반환
    @GetMapping("/posts/home/map")
    public ResponseEntity<List<PostDto>> showPostsHomeMap(@RequestParam("latitude") float latitude,
                                                          @RequestParam("longitude") float longitude, Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Long uid = principalDetails.getUser().getId();
        List<PostDto> posts = postService.getPostsHomeMap(latitude, longitude,uid);
        return ResponseEntity.ok(posts);
    }


    //Home-List 토글, postList 반환
    @GetMapping("/posts/home/list")
    public ResponseEntity<List<PostDto>> showPostsHomeList(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Long uid = principalDetails.getUser().getId();
        List<PostDto> posts = postService.getPostsHomeList(uid);
        return ResponseEntity.ok(posts);
    }
    //글 정보 반환, 조회수 ++
    @GetMapping("/postInfo")
    public ResponseEntity<PostDto> postInfo(@RequestParam("id") Long pid,Authentication authentication ) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Long uid = principalDetails.getUser().getId();
        PostDto pd = postService.getPostInfo(pid, uid);
        return ResponseEntity.ok(pd);
    }

    @PostMapping("/likeToggle")
    public ResponseEntity<String> toggleLike(@RequestParam long pid, @RequestParam long uid) {
        postService.toggleLike(pid, uid);
        return ResponseEntity.ok("Like toggled successfully");
    }


}

