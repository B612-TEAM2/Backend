package com.b6122.ping.domain;

import com.b6122.ping.dto.UserProfileResDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id")
    private User fromUser; //친구 요청을 보낸 User

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id")
    private User toUser; //친구 요청을 받은 User

    @Enumerated(EnumType.STRING)
    private FriendshipRequestStatus requestStatus; // PENDING, ACCEPTED, REJECTED

    private boolean isFriend = false;

    public void setIsFriend(boolean isFriend) {
        this.isFriend = isFriend;
    }

    public void setRequestStatus(FriendshipRequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }
    //친구 요청 시 메소드
    public static Friendship createFriendship(User fromUser, User toUser) {
        Friendship friendship = new Friendship();
        friendship.fromUser = fromUser;
        friendship.toUser = toUser;
        friendship.requestStatus = FriendshipRequestStatus.PENDING;
        return friendship;
    }

    //친구 상대방 유저 정보
    public UserProfileResDto getUserProfile(Long userId) {
        User fromUser = this.getFromUser();
        User toUser = this.getToUser();
        byte[] imageBytes;
        UserProfileResDto resDto;

        //사용자가 친구 요청을 했을 경우 친구 상대방은 toUser
        if (fromUser.getId().equals(userId)) {
            imageBytes = getByteArrayOfImageByPath(toUser.getProfileImagePath());
            resDto = new UserProfileResDto(toUser.getNickname(), imageBytes, toUser.getId());
            //사용자가 친구 요청을 받았을 경우 친구 상대방은 fromUser
        } else {
            imageBytes = getByteArrayOfImageByPath(fromUser.getProfileImagePath());
            resDto = new UserProfileResDto(fromUser.getNickname(), imageBytes, fromUser.getId());
        }
        return resDto;
    }

    /**
     * @param imagePath 서버의 이미지 저장 장소 경로
     * @return 이미지의 byte배열
     */
    public byte[] getByteArrayOfImageByPath(String imagePath) {
        try {
            Resource resource = new UrlResource(Path.of(imagePath).toUri());
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

