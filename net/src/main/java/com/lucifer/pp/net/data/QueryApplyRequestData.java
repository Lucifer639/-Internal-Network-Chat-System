package com.lucifer.pp.net.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryApplyRequestData {

    private String token;
    private Integer page;
    private Integer limit;

}
