package com.lucifer.pp.net.data;

import com.lucifer.pp.common.dto.GroupMember;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMemberData {
    private Long groupId;
    private GroupMember groupMember;
}
