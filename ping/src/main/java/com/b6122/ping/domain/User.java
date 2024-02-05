package com.b6122.ping.domain;

import com.b6122.ping.ImgPathProperties;
import com.b6122.ping.dto.UserProfileReqDto;
import com.b6122.ping.dto.UserProfileResDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private String nickname; // 사용자가 직접 입력하는 고유닉네임

    /** oauth2 연동 유저정보(username, providerId, provider) **/
    private String provider; //"google", "kakao", etc.
    private String providerId; //google, kakao 등 사용자의 고유Id
    private String username; // provider + _ + providerId

    private String profileImagePath;

    @Enumerated(EnumType.STRING)
    private UserRole role; // ROLE_USER or ROLE_ADMIN

    @OneToMany(mappedBy = "user")
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "fromUser", cascade = CascadeType.REMOVE)
    private List<Friendship> sentFriendshipRequests = new ArrayList<>();

    @OneToMany(mappedBy = "toUser", cascade = CascadeType.REMOVE)
    private List<Friendship> receivedFriendshipRequests = new ArrayList<>();

    //더미 데이터 용
    public static User createUser() {
        User user = new User();
        return user;
    }

    public void addPost(Post p) {//외부에서 post 생성시 posts list에 추가
        this.posts.add(p);
    }

    public void updateDummyProfile(String nickname, String profileImg) {
        this.profileImagePath = profileImg;
        this.nickname = nickname;
    }

    public void updateProfile(UserProfileReqDto reqDto) {
        String profileImgPath = saveProfileImageInStorage(reqDto.getProfileImg());
        this.nickname = reqDto.getNickname();
        this.profileImagePath = profileImgPath;
    }

    //회원 정보(nickname, profileImg, id)
    public UserProfileResDto getProfileInfo() {
        return new UserProfileResDto(nickname, this.getByteArrayOfProfileImgByPath(), id);
    }

    /**
     * 프로필 이미지를 서버 로컬 주소에 저장(배포 시 클라우드로 옮길 예정)
     * @param file 사용자가 업로드한 이미지 파일
     * @return 저장 장소의 절대 경로(디렉토리 경로 + 이미지 파일의 이름)
     * @throws IOException
     */
    private String saveProfileImageInStorage(MultipartFile file){

        //이미지 경로의 중복 방지를 위해 랜덤값으로 파일 명 저장
        String imageName = UUID.randomUUID() + file.getOriginalFilename();
        String path = ImgPathProperties.profileImgPath;

        File fileDir = new File(path);

        //지정한 디렉토리가 없으면 생성
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }

        //새로운 파일로 변환해서 지정한 경로에 저장.
        try {
            file.transferTo(new File(path, imageName));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //이미지 찾아올 때 파일 이름을 포함한 모든 경로가 필요하기 때문에 아래와 같이 저장.
        path = path + "\\" + imageName;

        return path;
    }

    /**
     * @return 이미지의 byte 배열
     */
    public byte[] getByteArrayOfProfileImgByPath() {
//        byte[] fileByteArray = Files.readAllBytes("파일의 절대경로");
        try {
            Resource resource = new UrlResource(Path.of(this.getProfileImagePath()).toUri());
            if (resource.exists() && resource.isReadable()) {
                // InputStream을 사용하여 byte 배열로 변환
                try (InputStream inputStream = resource.getInputStream()) {
                    byte[] data = new byte[inputStream.available()];
                    inputStream.read(data);
                    return data;
                }
            } else {
                // 이미지를 찾을 수 없는 경우 예외 또는 다른 처리 방법을 선택
                throw new RuntimeException("Image not found");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
