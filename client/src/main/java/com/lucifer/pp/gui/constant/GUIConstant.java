package com.lucifer.pp.gui.constant;

import cn.hutool.core.util.ObjectUtil;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

public class GUIConstant {
    public final static String DEFAULT_USER_AVATAR_STRING = "/gui/img/user/default_user.png";
    public final static String DEFAULT_GROUP_AVATAR_STRING = "/gui/img/group/avatar.jpg";
    public final static String NOTICE_NEW_IMAGE_STRING = "/gui/img/common/notice-new.jpg";
    public final static String NOTICE_IMAGE_STRING = "/gui/img/common/notice.jpg";
    public final static String CHAT_NEW_IMAGE_STRING = "/gui/img/common/chat-new.png";
    public final static String CHAT_IMAGE_STRING = "/gui/img/common/chat.png";
    public final static String EMOJI_IMAGE_STRING = "/gui/img/common/emoji.jpg";
    public final static String FILE_IMAGE_STRING = "/gui/img/common/file.jpg";
    public final static String ADD_GROUP_IMAGE_STRING = "/gui/img/common/add_group.jpg";
    public final static List<String> EMOJI_BASE_64 = new ArrayList<>();
    public final static String IMAGE_BASE64_PREFIX = "data:image/png;base64,";
    public final static String EMOJI_IMAGE_ATTR_KEY = "emoji-index";
    //聊天记录之间时间间隔多久插入一次时间label（单位：毫秒）
    public final static long TIME_LABEL_INTERVAL = 10 * 60 * 1000;
    //表情正则
    public final static Pattern EMOJI_PATTERN = Pattern.compile("\\[\\[emoji:.*?]]");
    public final static ImageView NOTICE_NEW_IMAGE = new ImageView(new Image(Objects.requireNonNull(GUIConstant.class.getResourceAsStream(GUIConstant.NOTICE_NEW_IMAGE_STRING))));
    public final static ImageView NOTICE_IMAGE = new ImageView(new Image(Objects.requireNonNull(GUIConstant.class.getResourceAsStream(GUIConstant.NOTICE_IMAGE_STRING))));
    public final static ImageView CHAT_NEW_IMAGE = new ImageView(new Image(Objects.requireNonNull(GUIConstant.class.getResourceAsStream(GUIConstant.CHAT_NEW_IMAGE_STRING))));
    public final static ImageView CHAT_IMAGE = new ImageView(new Image(Objects.requireNonNull(GUIConstant.class.getResourceAsStream(GUIConstant.CHAT_IMAGE_STRING))));
    public final static ImageView ADD_GROUP_IMAGE = new ImageView(new Image(Objects.requireNonNull(GUIConstant.class.getResourceAsStream(GUIConstant.ADD_GROUP_IMAGE_STRING))));
    static {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:/gui/img/emoji/*.png");
            for (Resource resource : resources){
                try(InputStream inputStream = resource.getInputStream()){
                    byte[] bytes = inputStream.readAllBytes();
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    EMOJI_BASE_64.add(base64);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public static ImageView initUserAvatar(String base64){
        if (ObjectUtil.isEmpty(base64)){
            return new ImageView(new Image(Objects.requireNonNull(GUIConstant.class.getResourceAsStream(GUIConstant.DEFAULT_USER_AVATAR_STRING))));
        }else{
            byte[] bytes = Base64.getDecoder().decode(base64);
            Image image = new Image(new ByteArrayInputStream(bytes));
            return new ImageView(image);
        }
    }
}
