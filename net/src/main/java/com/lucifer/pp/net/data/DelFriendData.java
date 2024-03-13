package com.lucifer.pp.net.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DelFriendData {
    private String token;
    private Long to;
}
