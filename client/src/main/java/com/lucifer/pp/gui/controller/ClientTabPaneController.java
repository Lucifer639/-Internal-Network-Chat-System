package com.lucifer.pp.gui.controller;

import cn.hutool.core.util.ObjectUtil;
import com.lucifer.pp.ClientApplication;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.client.util.NetUtil;
import com.lucifer.pp.common.dto.Friend;
import com.lucifer.pp.common.dto.Group;
import com.lucifer.pp.common.dto.GroupMember;
import com.lucifer.pp.gui.constant.GUIConstant;
import com.lucifer.pp.gui.util.AlertGenerator;
import com.lucifer.pp.gui.view.ChatView;
import com.lucifer.pp.net.data.DelFriendData;
import com.lucifer.pp.net.data.DisbandGroupData;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.data.QuitGroupData;
import com.lucifer.pp.net.netenum.ChatEnum;
import com.lucifer.pp.net.netenum.GroupMemberLevel;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import de.felixroske.jfxsupport.FXMLController;
import jakarta.annotation.Resource;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import org.springframework.context.ApplicationContext;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.*;

@FXMLController
public class ClientTabPaneController implements Initializable {


    @FXML
    public TableView<Friend> friendList;
    @FXML
    public TableView<Group> groupList;

    private final ContextMenu friendMenu = new ContextMenu();
    private final MenuItem friendChat = new MenuItem("聊天");
    private final MenuItem deleteFriend = new MenuItem("删除好友");

    private final ContextMenu groupMenu = new ContextMenu();
    private final MenuItem groupChat = new MenuItem("聊天");
    private final MenuItem deleteGroup = new MenuItem("解散/退出群");

    @Resource
    NetUtil netUtil;

    @Resource
    ApplicationContext applicationContext;

    @Override
    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initMenu();

        friendList.setItems(PPClientContext.friends);
        TableColumn<Friend, Friend> friendColumn = new TableColumn<>();
        friendColumn.setCellValueFactory((param)-> new SimpleObjectProperty<>(param.getValue()));
        friendColumn.setCellFactory(friendTableColumn-> new TableCell<>(){

            @Override
            protected void updateItem(Friend friend,boolean empty){
                super.updateItem(friend,empty);
                if (ObjectUtil.isNotEmpty(friend) && !empty){
                    BorderPane borderPane = new BorderPane();
                    ImageView imageView;
                    if (ObjectUtil.isEmpty(friend.getAvatar())){
                        imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(GUIConstant.DEFAULT_USER_AVATAR_STRING))));
                    }else{
                        byte[] imageBytes = Base64.getDecoder().decode(friend.getAvatar());
                        imageView = new ImageView(new Image(new ByteArrayInputStream(imageBytes)));
                    }
                    imageView.setFitWidth(80);
                    imageView.setFitHeight(80);
                    borderPane.setLeft(imageView);

                    Label nameLabel = new Label(friend.getName());
                    Label onLineLabel = new Label(friend.isOnline()?"在线":"离线");
                    borderPane.setCenter(nameLabel);
                    borderPane.setRight(onLineLabel);

                    this.setGraphic(borderPane);
                }else{
                    this.setGraphic(null);
                }
            }
        });

        GUIConstant.showContextMenuOnTableView(friendList,friendMenu);

        friendColumn.prefWidthProperty().bind(friendList.widthProperty());
        friendList.getColumns().add(friendColumn);
        friendList.setRowFactory((tv)->{
            TableRow<Friend> row = new TableRow<>();
            row.setMinHeight(80);
            row.setMaxHeight(80);
            return row;
        });

        groupList.setItems(PPClientContext.groups);
        TableColumn<Group,Group> groupColumn = new TableColumn<>();
        groupColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        groupColumn.setCellFactory(groupTableColumn -> new TableCell<>(){
            @Override
            protected void updateItem(Group group, boolean empty) {
                super.updateItem(group, empty);
                if (ObjectUtil.isNotEmpty(group) && !empty){
                    BorderPane borderPane = new BorderPane();
                    ImageView imageView;
                    if (ObjectUtil.isEmpty(group.getAvatar())){
                        imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(GUIConstant.DEFAULT_GROUP_AVATAR_STRING))));

                    }else{
                        byte[] imageBytes = Base64.getDecoder().decode(group.getAvatar());
                        imageView = new ImageView(new Image(new ByteArrayInputStream(imageBytes)));
                    }
                    imageView.setFitWidth(80);
                    imageView.setFitHeight(80);
                    borderPane.setLeft(imageView);

                    Label nameLabel = new Label(group.getName());
                    borderPane.setCenter(nameLabel);

                    this.setGraphic(borderPane);
                }else{
                    this.setGraphic(null);
                }
            }
        });

        GUIConstant.showContextMenuOnTableView(groupList,groupMenu);

        groupColumn.prefWidthProperty().bind(groupList.widthProperty());
        groupList.getColumns().add(groupColumn);
        groupList.setRowFactory((tv)->{
            TableRow<Group> row = new TableRow<>();
            row.setMinHeight(80);
            row.setMaxHeight(80);
            return row;
        });
    }

    private void initMenu(){
        friendMenu.getItems().addAll(friendChat,deleteFriend);
        groupMenu.getItems().addAll(groupChat,deleteGroup);

        friendChat.setOnAction(actionEvent -> {
            int index = friendList.getSelectionModel().getSelectedIndex();
            openChatWindow(PPClientContext.friends.get(index).getId(),ChatEnum.FRIEND);
        });

        deleteFriend.setOnAction(actionEvent -> {
            Optional<ButtonType> result = AlertGenerator.showConfirm("确定删除该好友?");
            result.ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK){
                    int index = friendList.getSelectionModel().getSelectedIndex();
                    PPProtocol<DelFriendData> protocol = new PPProtocol<>();
                    DelFriendData data = new DelFriendData(PPClientContext.token,PPClientContext.friends.get(index).getId());
                    protocol.setPpProtocol(PPProtocolEnum.DELETE_FRIEND);
                    protocol.setData(data);
                    netUtil.sendMessage(protocol);
                    PPClientContext.friends.remove(index);
                }
            });
        });

        groupChat.setOnAction(actionEvent -> {
            int index = groupList.getSelectionModel().getSelectedIndex();
            openChatWindow(PPClientContext.groups.get(index).getId(),ChatEnum.GROUP);
        });

        deleteGroup.setOnAction(actionEvent -> {
            Optional<ButtonType> res = AlertGenerator.showConfirm("确定解散/退出群?");
            res.ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK){
                    int index = groupList.getSelectionModel().getSelectedIndex();
                    Group group = PPClientContext.groups.get(index);
                    Optional<GroupMember> me = group.getMembers().stream()
                            .filter(groupMember -> groupMember.getId().equals(PPClientContext.uid))
                            .findFirst();
                    me.ifPresent(groupMember -> {
                        if (groupMember.getLevel().equals(GroupMemberLevel.LEADER.level)){
                            DisbandGroupData data = new DisbandGroupData(PPClientContext.token,group.getId());
                            PPProtocol<DisbandGroupData> ppProtocol = new PPProtocol<>(PPProtocolEnum.DISBAND_GROUP,data);
                            netUtil.sendMessage(ppProtocol);
                        }else{
                            QuitGroupData data = new QuitGroupData(PPClientContext.token,group.getId());
                            PPProtocol<QuitGroupData> ppProtocol = new PPProtocol<>(PPProtocolEnum.QUIT_GROUP,data);
                            netUtil.sendMessage(ppProtocol);
                            PPClientContext.groups.remove(group);
                            groupList.refresh();
                        }
                    });
                }
            });
        });
    }

    // 打开聊天窗口
    public void openChatWindow(Long id, ChatEnum chatEnum){
        Optional<Object[]> first = PPClientContext.newChats.stream()
                .filter(newChat -> newChat[0].equals(id) && newChat[1].equals(chatEnum.code)).findFirst();
        Object[] newChat = first.orElse(new Object[]{id,chatEnum.code});
        ChatPaneController chatPaneController = applicationContext.getBean(ChatPaneController.class);
        if (!PPClientContext.chatWindowExist){
            PPClientContext.chatWindowExist = true;
            first.ifPresentOrElse(chat -> {}, () -> PPClientContext.newChats.add(newChat));
            if (PPClientContext.chatWindowInitialized){
                chatPaneController.addChat(newChat);
            }else{
                PPClientContext.chatWindowInitialized = true;
            }
            ClientApplication.showView(ChatView.class, Modality.NONE,(windowEvent)->{
                chatPaneController.chatPaneMap.clear();
                chatPaneController.chatArea.setCenter(null);
                chatPaneController.currentChatPane = null;
                PPClientContext.chatWindowExist = false;
                PPClientContext.newChats.clear();
                System.gc();
                return null;
            });
        }else{
            first.ifPresentOrElse(chat -> {}, () -> {
                PPClientContext.newChats.add(newChat);
                Platform.runLater(() -> chatPaneController.addChat(newChat));
            });
        }
    }
}
