package com.lucifer.pp.client.function;

import com.lucifer.pp.net.netenum.PPProtocolEnum;

import java.util.function.Function;

public interface PPFunction extends Function<Object,Object> {
    PPProtocolEnum getProtocol();
}
