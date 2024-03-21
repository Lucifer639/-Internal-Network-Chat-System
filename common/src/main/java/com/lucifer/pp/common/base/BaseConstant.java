package com.lucifer.pp.common.base;

public class BaseConstant {

    public final static Integer ENTITY_STATUS_VALID = 1;
    public final static Integer ENTITY_STATUS_INVALID = 0;
    public final static int CLIENT_PORT = 6667;
    public final static String REDIS_ONLINE_USER_KEY = "onlineUser";
    public final static String REDIS_LOCK_USER_KEY = "lockUser";
    public final static String REDIS_LOCKED_USER_KEY = "lockedUser";
    // 单位:毫秒
    public final static long TOKEN_EXPIRE = 24 * 3600 * 1000;
    // 心跳包检查间隔，单位：毫秒
    public final static long HEART_BEAT_CHECK_TIME = 10 * 1000;
    // 客户端发送心跳包间隔，单位：毫秒
    public final static long CLIENT_SEND_HEAR_BEAT_TIME = 5 * 1000;
    // 输错密码多少次锁定用户
    public final static int TIMES_TO_LOCK = 5;
    // 锁定用户时长(毫秒)
    public final static long LOCK_TIME = 30 * 1000;
    public final static int DEFAULT_FRIEND_LIMIT = 5;
    // 默认申请/邀请分页大小
    public final static int DEFAULT_APPLICATION_LIMIT = 5;
    // 默认好友聊天记录分页大小
    public final static int DEFAULT_FRIEND_CHAT_LIMIT = 10;
    // 默认群聊记录分页大小
    public final static int DEFAULT_GROUP_CHAT_LIMIT = 10;
    // 用户配置文件路径
    public final static String USER_CONFIG_PATH = "./localConfig";
    // 用户配置文件名
    public final static String USER_CONFIG_FILE_NAME = "user.config";
    // 距离token过期前x毫秒刷新token
    public final static long REFRESH_TIME_BEFORE_TOKEN_EXPIRE = 30 * 60 * 1000;
    // 权限码相关
    public final static String ALL_PERMISSION_CODE = "ALL_PERMISSION";
    public final static String ADD_GROUP_MEMBER_CODE = "ADD_GROUP_MEMBER";
    public final static String DELETE_GROUP_MEMBER_CODE = "DELETE_GROUP_MEMBER";
    public final static String UPDATE_GROUP_MEMBER_CODE = "UPDATE_GROUP_MEMBER";
    public final static String GROUP_CHAT_SILENCE_CODE = "GROUP_CHAT_SILENCE";
    public final static String DISBAND_GROUP_CODE = "DISBAND_GROUP";
}
