package com.lucifer.pp.server.function;

import com.lucifer.pp.net.netenum.PPProtocolEnum;

import java.util.function.Function;

public interface PPFunction extends Function<Object,Object> {
    PPProtocolEnum getProtocol();
}
