package com.lucifer.pp.net.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateGroupResponseData {
    private Long groupId;
    private String groupName;
}
