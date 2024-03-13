package com.lucifer.pp.gui.controller;

import cn.hutool.core.util.ObjectUtil;
import com.lucifer.pp.ClientApplication;
import com.lucifer.pp.client.PPClient;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.client.util.NetUtil;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.properties.ServerProperties;
import com.lucifer.pp.gui.constant.GUIConstant;
import com.lucifer.pp.gui.util.AlertGenerator;
import com.lucifer.pp.gui.view.ChatView;
import com.lucifer.pp.gui.view.NoticeView;
import com.lucifer.pp.gui.view.SearchView;
import com.lucifer.pp.net.data.CreateGroupRequestData;
import com.lucifer.pp.net.data.HeartBeatData;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.data.QueryApplyRequestData;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import de.felixroske.jfxsupport.FXMLController;
import jakarta.annotation.Resource;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

@FXMLController
public class ClientPaneController implements Initializable {

    @FXML
    ImageView avatar;

    @FXML
    Label searchLabel;

    @FXML
    public Label noticeLabel;

    @FXML
    public Label chatLabel;

    @FXML
    public Label addGroupLabel;

    @Resource
    PPClient ppClient;

    @Resource
    NetUtil netUtil;

    @Resource
    ServerProperties serverProperties;

    @Resource
    ApplicationContext applicationContext;

    private final HeartBeatData heartBeatData = new HeartBeatData();

    @FXML
    Label name;

    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        name.setText(PPClientContext.name);
        if (ObjectUtil.isNotEmpty(PPClientContext.avatar)){
            avatar.setImage(new Image(PPClientContext.avatar));
        }

        searchLabel.setTooltip(new Tooltip("查找好友/群"));
        searchLabel.setCursor(Cursor.HAND);
        noticeLabel.setTooltip(new Tooltip("消息通知"));
        noticeLabel.setCursor(Cursor.HAND);
        chatLabel.setTooltip(new Tooltip("聊天"));
        chatLabel.setCursor(Cursor.HAND);
        addGroupLabel.setTooltip(new Tooltip("创建群聊"));
        addGroupLabel.setCursor(Cursor.HAND);

        PPClientContext.heartBeatFuture = PPClientContext.heartBeatExecutor.scheduleAtFixedRate(this::heartBeat,
            BaseConstant.CLIENT_SEND_HEAR_BEAT_TIME,
            BaseConstant.CLIENT_SEND_HEAR_BEAT_TIME, TimeUnit.MILLISECONDS);

        try{
            InputStream ins = new FileInputStream(BaseConstant.USER_CONFIG_PATH+"/"+BaseConstant.USER_CONFIG_FILE_NAME);
            Properties prop = new Properties();
            prop.load(ins);
            if (ObjectUtil.isNotEmpty(prop.getProperty("ignoredApplication")) && Boolean.parseBoolean(prop.getProperty("ignoredApplication"))){
                ImageView imageView = GUIConstant.NOTICE_NEW_IMAGE;
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                noticeLabel.setGraphic(imageView);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        ClientApplication.getStage().setOnCloseRequest(windowEvent -> {
            Map<String,String> data = new HashMap<>();
            data.put("token",PPClientContext.token);
            PPProtocol<Map<String,String>> ppProtocol = new PPProtocol<>(PPProtocolEnum.LOGOUT,data);
            netUtil.sendMessage(ppProtocol);
            ppClient.future.channel().close();
            if (ObjectUtil.isNotEmpty(PPClientContext.heartBeatFuture)){
                PPClientContext.heartBeatFuture.cancel(true);
            }
            PPClientContext.heartBeatExecutor.shutdown();
            try {
                InputStream ins = new FileInputStream(BaseConstant.USER_CONFIG_PATH+"/"+BaseConstant.USER_CONFIG_FILE_NAME);
                Properties prop = new Properties();
                prop.load(ins);
                prop.keySet().forEach(key -> {
                    prop.setProperty(key.toString(),prop.getProperty(key.toString()));
                });
                prop.setProperty("ignoredApplication",String.valueOf(PPClientContext.ignoredApplication));
                OutputStream ops = new FileOutputStream(BaseConstant.USER_CONFIG_PATH+"/"+BaseConstant.USER_CONFIG_FILE_NAME);
                prop.store(ops,"");
                ins.close();
                ops.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            Platform.exit();
        });
    }

    @FXML
    public void openSearchWindow(){
        ClientApplication.showView(SearchView.class,Modality.APPLICATION_MODAL,(windowEvent)->{
            PPClientContext.searchData = null;
            return null;
        });
    }

    @FXML
    public void openNoticeWindow(){
        PPProtocol<QueryApplyRequestData> ppProtocol = new PPProtocol<>();
        ppProtocol.setPpProtocol(PPProtocolEnum.QUERY_APPLY_REQUEST);
        QueryApplyRequestData data = new QueryApplyRequestData(PPClientContext.token, 1,BaseConstant.DEFAULT_APPLICATION_LIMIT);
        ppProtocol.setData(data);
        netUtil.sendMessage(ppProtocol);
        PPClientContext.noticeWindowExist = true;
        ImageView imageView = GUIConstant.NOTICE_IMAGE;
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);
        noticeLabel.setGraphic(imageView);
        ClientApplication.showView(NoticeView.class,Modality.APPLICATION_MODAL,(windowEvent)->{
            PPClientContext.applicationPageInfo = null;
            long count = PPClientContext.applications.stream().
                    filter(application -> ObjectUtil.isEmpty(application.getAgree()))
                    .count();
            if (count > 0) PPClientContext.ignoredApplication = true;
            PPClientContext.applications = null;
            PPClientContext.noticeWindowExist = false;
            return null;
        });
    }

    @FXML
    public void openChatWindow(){
        if (ObjectUtil.isEmpty(PPClientContext.newChats) || PPClientContext.chatWindowExist) return;
        PPClientContext.chatWindowExist = true;
        ImageView chatImage = GUIConstant.CHAT_IMAGE;
        chatImage.setFitWidth(50);
        chatImage.setFitHeight(50);
        chatLabel.setGraphic(chatImage);
        if (PPClientContext.chatWindowInitialized){
            ChatPaneController chatPaneController = applicationContext.getBean(ChatPaneController.class);
            PPClientContext.newChats.forEach(chatPaneController::addChat);
        }else{
            PPClientContext.chatWindowInitialized = true;
        }
        ClientApplication.showView(ChatView.class,Modality.NONE,(windowEvent)->{
            ChatPaneController chatPaneController = applicationContext.getBean(ChatPaneController.class);
            chatPaneController.chatPaneMap.clear();
            chatPaneController.chatArea.setCenter(null);
            chatPaneController.currentChatPane = null;
            PPClientContext.chatWindowExist = false;
            PPClientContext.newChats.clear();
            System.gc();
            return null;
        });
    }

    @FXML
    public void createGroup(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("创建群聊");
        dialog.setHeaderText("输入群名称");
        TextField input = new TextField();
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(input);
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty()) return;
        if (result.get() == ButtonType.OK){
            if (ObjectUtil.isEmpty(input.getText())){
                AlertGenerator.showError("群名称不能为空!");
                return;
            }
            CreateGroupRequestData data = new CreateGroupRequestData(PPClientContext.token,input.getText());
            PPProtocol<CreateGroupRequestData> ppProtocol = PPProtocol.of(PPProtocolEnum.CREATE_GROUP_REQUEST,data);
            netUtil.sendMessage(ppProtocol);
        }
    }

    public void sendHeartBeat() throws Exception{
        heartBeatData.setToken(PPClientContext.token);
        PPProtocol<HeartBeatData> ppProtocol = new PPProtocol<>(PPProtocolEnum.HEART_BEAT,heartBeatData);
        CompletableFuture<String> future = netUtil.sendMessage(ppProtocol);
        future.get();
    }

    public void heartBeat(){
        try {
            sendHeartBeat();
        }catch (Exception e){
            System.out.println("与服务器断开链接...");
            reconnect();
        }
    }

    public void reconnect(){
        try {
            sendHeartBeat();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
