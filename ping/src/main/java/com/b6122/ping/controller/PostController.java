package com.b6122.ping.controller;

import com.b6122.ping.auth.PrincipalDetails;
import com.b6122.ping.dto.PostDto;
import com.b6122.ping.service.PostService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Getter@Setter
@RestController
@RequestMapping("/posts/home")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    //글 작성 후 디비 저장
    @PostMapping("/store")
    public ResponseEntity getPost(@RequestBody @Validated PostDto postDto){
        Long pid = postService.createPost(postDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pid);
    }

    //글 수정 휴 디비 저장
    @PostMapping("/edit")
    public ResponseEntity modifyPost(@RequestBody @Validated PostDto postDto){
        Long pid = postService.modifyPost(postDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pid);
    }

    //Home-Map, 내 모든 글의 pin 반환
    @GetMapping
    public ResponseEntity<List<PostDto>> showPinsHome(@RequestParam("latitude") float latitude,
                                                          @RequestParam("longitude") float longitude, Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Long uid = principalDetails.getUser().getId();
        List<PostDto> posts = postService.getPinsHomeMap(uid);
        return ResponseEntity.ok(posts);
    }

    //Home-Map 클릭, postList 반환
    @GetMapping("/map")
    public ResponseEntity<List<PostDto>> showPostsHomeMap(@RequestParam("latitude") float latitude,
                                                          @RequestParam("longitude") float longitude, Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Long uid = principalDetails.getUser().getId();
        List<PostDto> posts = postService.getPostsHomeMap(latitude, longitude,uid);
        return ResponseEntity.ok(posts);
    }


    //Home-List 토글, postList 반환
    @GetMapping("/list")
    public ResponseEntity<List<PostDto>> showPostsHomeList(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        Long uid = principalDetails.getUser().getId();
        List<PostDto> posts = postService.getPostsHomeList(uid);
        return ResponseEntity.ok(posts);
    }
    //글 수정 요청시 디비에서 반환


    //내 글 보기

    //친구 글 보기
    //public 글 보기
}

