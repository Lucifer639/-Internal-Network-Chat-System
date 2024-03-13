package com.lucifer.pp.net.data;

import com.lucifer.pp.net.netenum.PPProtocolEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PPProtocol<T> {
    private PPProtocolEnum ppProtocol;
    private T data;

    public static <K> PPProtocol<K> of(PPProtocolEnum ppProtocol,K data){
        return new PPProtocol<>(ppProtocol, data);
    }
}
