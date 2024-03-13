package com.lucifer.pp.net.data;

import com.lucifer.pp.net.netenum.SearchType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequestData {
    private SearchType searchType;
    private String searchWord;
    private String token;
}
