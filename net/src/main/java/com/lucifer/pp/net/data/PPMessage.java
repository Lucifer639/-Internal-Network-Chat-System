package com.lucifer.pp.net.data;

import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.net.netenum.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PPMessage {
    private PPProtocolEnum originProtocol;
    private StatusEnum statusEnum;
    private String message;
}
