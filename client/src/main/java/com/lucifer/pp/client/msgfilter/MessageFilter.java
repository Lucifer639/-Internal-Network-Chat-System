package com.lucifer.pp.client.msgfilter;

import java.util.function.Function;

public interface MessageFilter {
    String encode(String s);
    String decode(String s);
}
