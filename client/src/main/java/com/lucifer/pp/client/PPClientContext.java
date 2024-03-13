package com.lucifer.pp.client;

import cn.hutool.core.lang.tree.Node;
import com.github.pagehelper.PageInfo;
import com.lucifer.pp.common.dto.Application;
import com.lucifer.pp.common.dto.Friend;
import com.lucifer.pp.common.dto.Group;
import com.lucifer.pp.net.data.SearchResponseData;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;


public class PPClientContext {
    public static Long uid;
    public static String userCode;
    public static String avatar;
    public static String name;
    public static String token;
    public static ObservableList<Friend> friends;
    public static ObservableList<Group> groups;
    public static ObservableList<Application> applications;
    public static ScheduledExecutorService heartBeatExecutor = Executors.newScheduledThreadPool(1);
    public static ScheduledFuture<?> heartBeatFuture;
    public static ObservableList<SearchResponseData> searchData;
    public static PageInfo applicationPageInfo;
    public static boolean noticeWindowExist = false;
    public static boolean ignoredApplication = false;
    public static boolean chatWindowExist = false;
    public static boolean chatWindowInitialized = false;
    /**
     *   obj[0]为id,obj[1]为聊天类型枚举 0 or 1(friend or group)
     *   当obj[1]为1时,obj[0]为群id
     */
    public static ObservableList<Object[]> newChats;
    public static Map<Long,PageInfo> friendChatPages = new HashMap<>();
    public static Map<Long,PageInfo> groupChatPages = new HashMap<>();
}
