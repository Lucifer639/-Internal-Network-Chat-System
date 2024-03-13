package com.lucifer.pp.net.netenum;

public enum PPProtocolEnum {
    MESSAGE,
    LOGIN_REQUEST,LOGIN_RESPONSE,RE_LOGIN,
    REGISTER_REQUEST,REGISTER_RESPONSE,
    DELETE_FRIEND,FRIEND_ONLINE,FRIEND_OFFLINE,ADD_FRIEND,
    ADD_GROUP,ADD_GROUP_MEMBER,DISBAND_GROUP,DELETE_GROUP_MEMBER,QUIT_GROUP,
    CREATE_GROUP_REQUEST,CREATE_GROUP_RESPONSE,
    SEARCH_REQUEST,SEARCH_RESPONSE,
    APPLY_REQUEST,APPLY_RESPONSE,QUERY_APPLY_REQUEST,QUERY_APPLY_RESPONSE,
    FRIEND_CHAT, QUERY_FRIEND_CHAT_REQUEST,QUERY_FRIEND_CHAT_RESPONSE,
    GROUP_CHAT, QUERY_GROUP_CHAT_REQUEST,QUERY_GROUP_CHAT_RESPONSE,
    HEART_BEAT,
    LOGOUT,REFRESH_TOKEN,
}