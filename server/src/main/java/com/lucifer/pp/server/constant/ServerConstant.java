package com.lucifer.pp.server.constant;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ServerConstant {

    public final static int NET_UTIL_CORE_POOL_SIZE = 20;
    public final static int NET_UTIL_MAX_POOL_SIZE = 30;
    // 单位：毫秒
    public final static long NET_UTIL_KEEP_ALIVE_TIME =  2 * 1000;
    public final static BlockingQueue<Runnable> NET_UTIL_BLOCK_QUEUE = new ArrayBlockingQueue<>(30);
}
