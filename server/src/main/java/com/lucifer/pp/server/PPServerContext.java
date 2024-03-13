package com.lucifer.pp.server;

import com.lucifer.pp.server.constant.ServerConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PPServerContext {
    public static final ReadWriteLock heartBeatLock = new ReentrantReadWriteLock();
    public static final ThreadPoolExecutor netUtilThreadPool = new ThreadPoolExecutor(
            ServerConstant.NET_UTIL_CORE_POOL_SIZE,
            ServerConstant.NET_UTIL_MAX_POOL_SIZE,
            ServerConstant.NET_UTIL_KEEP_ALIVE_TIME,
            TimeUnit.MILLISECONDS,
            ServerConstant.NET_UTIL_BLOCK_QUEUE,
            new ThreadPoolExecutor.DiscardPolicy()
    );
}
