package com.lucifer.pp.net.data;

import com.lucifer.pp.net.netenum.GroupMemberLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LevelUpdateMemberData {
    private String token;
    private Long groupId;
    private Long memberId;
    private GroupMemberLevel memberLevel;
}
