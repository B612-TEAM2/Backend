package com.b6122.ping.dto;

import com.b6122.ping.domain.FriendshipRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateFriendshipDto {
    private FriendshipRequestStatus requestStatus;
    private boolean isFriend;
}
