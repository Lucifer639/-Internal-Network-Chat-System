package com.lucifer.pp.net.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
//此类应以List形式放在PPProtocol的data字段
public class SearchResponseData {
    private Long id;
    private String avatar;
    //当搜索类型为SearchType.GROUP时，此字段为群主手机号
    private String userCode;
    private String name;
    //当搜索类型为SearchType.FRIEND时此字段有效
    private boolean isOnline;
}
