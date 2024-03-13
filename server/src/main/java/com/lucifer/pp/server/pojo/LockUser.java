package com.lucifer.pp.server.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LockUser implements Serializable {
    private Long id;
    private int errorCount;
}
