package com.lucifer.pp.gui.controller;

import cn.hutool.core.util.ObjectUtil;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.client.msgfilter.MessageFilter;
import com.lucifer.pp.client.util.NetUtil;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.dto.Friend;
import com.lucifer.pp.common.dto.Group;
import com.lucifer.pp.common.dto.GroupMember;
import com.lucifer.pp.gui.constant.GUIConstant;
import com.lucifer.pp.gui.util.AlertGenerator;
import com.lucifer.pp.net.data.*;
import com.lucifer.pp.net.netenum.ChatEnum;
import com.lucifer.pp.net.netenum.GroupMemberLevel;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import de.felixroske.jfxsupport.FXMLController;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.web.HTMLEditor;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;

@FXMLController
public class ChatPaneController implements Initializable {

//    @FXML
//    SplitPane splitPane;

    @FXML
    BorderPane splitPane;

    @FXML
    TableView<Object[]> chatTable;

    @FXML
    BorderPane chatArea;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    NetUtil netUtil;

    public Map<Long,BorderPane> chatPaneMap = new Hashtable<>();
    ScrollPane emojiScrollPane = new ScrollPane();
    Scene emojiScene = new Scene(emojiScrollPane);
    BorderPane currentChatPane;
    StringBuilder sb = new StringBuilder();

    double emojiScrollPaneWidth = 400;
    double emojiScrollPaneHeight = 200;
    double emojiWidth = 50;
    ChatHistoryScrollListener chatHistoryScrollListener = new ChatHistoryScrollListener();
    ContextMenu groupFunctionMenu = new ContextMenu();
    MenuItem showMember = new MenuItem("显示群成员");
    MenuItem invite = new MenuItem("邀请好友入群");

    ContextMenu memberFunctionMenu = new ContextMenu();
    MenuItem levelUp = new MenuItem("提升为管理员");
    MenuItem levelDown = new MenuItem("降级为普通成员");
    MenuItem kick = new MenuItem("踢出群");
    MenuItem silence = new MenuItem("禁言");

    Long currentMemberId;
    ObservableList<GroupMember> currentMemberList = null;
    TableView<GroupMember> currentMemberTable = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chatTable.setItems(PPClientContext.newChats);
        TableColumn<Object[], Object[]> chatColumn = new TableColumn<>();
        chatColumn.setCellValueFactory((param)-> new SimpleObjectProperty<>(param.getValue()));
        chatColumn.setCellFactory(chatTableColumn-> new TableCell<>(){
            @Override
            protected void updateItem(Object[] newChat,boolean empty){
                super.updateItem(newChat,empty);
                if (ObjectUtil.isNotEmpty(newChat) && !empty){
                    BorderPane borderPane = new BorderPane();
                    Label label = initNameLabel(newChat);
                    Button button = new Button("x");
                    borderPane.setCenter(label);
                    borderPane.setRight(button);
                    this.setGraphic(borderPane);
                }else{
                    this.setGraphic(null);
                }
            }
        });

        chatColumn.prefWidthProperty().bind(chatTable.widthProperty());
        chatTable.getColumns().add(chatColumn);
        chatTable.setRowFactory((tv)->{
            TableRow<Object[]> row = new TableRow<>();
            row.setMinHeight(80);
            row.setMaxHeight(80);
            return row;
        });

        chatTable.setOnMouseClicked(mouseEvent -> {
            if (ObjectUtil.isEmpty(PPClientContext.newChats)) return;
            int index = chatTable.getSelectionModel().getSelectedIndex();
            index = index == -1? 0:index;
            Long id = (Long) PPClientContext.newChats.get(index)[0];
            currentChatPane = chatPaneMap.get(id);
            chatArea.setCenter(currentChatPane);
        });

        initGroupFunctionMenu();
        initMemberFunctionMenu();
        initEmojiList();
        initChatPane();
    }

    private Label initNameLabel(Object[] newChat) {
        Label label = new Label();
        if (newChat[1].equals(ChatEnum.FRIEND.code)){
            Optional<Friend> optionalFriend = PPClientContext.friends.stream()
                    .filter(friend -> friend.getId().equals(newChat[0]))
                    .findFirst();
            optionalFriend.ifPresent(friend -> label.setText(friend.getName()));
        }else{
            Optional<Group> optionalGroup = PPClientContext.groups.stream()
                    .filter(group -> group.getId().equals(newChat[0]))
                    .findFirst();
            optionalGroup.ifPresent(group -> label.setText(group.getName()));
        }
        return label;
    }

    private void initChatPane(){
        if (ObjectUtil.isEmpty(PPClientContext.newChats)) return;

        PPClientContext.newChats.forEach(this::addChat);
        Optional<Long> first = chatPaneMap.keySet().stream().findFirst();
        first.ifPresent(id -> {
            currentChatPane = chatPaneMap.get(id);
            chatArea.setCenter(currentChatPane);
        });
    }

    public void addChat(Object[] newChat){
        if (chatPaneMap.containsKey((Long) newChat[0])) return;
        BorderPane borderPane = new BorderPane();
        chatPaneMap.put((Long) newChat[0],borderPane);
        ScrollPane chatHistoryScrollPane = new ScrollPane();
        TextFlow chatHistory = new TextFlow();
        HTMLEditor inputArea = new HTMLEditor();
        HBox functionArea = new HBox();
        borderPane.heightProperty().addListener((observableValue, height, newHeight) -> {
            chatHistory.setPrefHeight(Double.parseDouble(newHeight.toString()) * 0.65);
            inputArea.setPrefHeight(Double.parseDouble(newHeight.toString()) * 0.25);
            functionArea.setPrefHeight(Double.parseDouble(newHeight.toString()) * 0.1);
        });
        chatHistoryScrollPane.setContent(chatHistory);
        chatHistory.prefWidthProperty().bind(borderPane.widthProperty().subtract(20));
        borderPane.setTop(chatHistoryScrollPane);

        inputArea.setPrefWidth(borderPane.getPrefWidth());
        borderPane.setCenter(inputArea);
        for (Node node : inputArea.lookupAll(".tool-bar")) {
            // 将工具栏节点从HTMLEditor中移除
            node.setVisible(false);
            node.setManaged(false);
        }

        Button button = new Button("发送");
        button.setOnAction(actionEvent -> sendMsg((Long) newChat[0], (Integer) newChat[1]));
        ImageView emojiImage = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(GUIConstant.EMOJI_IMAGE_STRING))));
        ImageView fileImage = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(GUIConstant.FILE_IMAGE_STRING))));
        emojiImage.setFitWidth(50);
        emojiImage.setFitHeight(50);
        fileImage.setFitHeight(50);
        fileImage.setFitWidth(50);
        emojiImage.setOnMouseMoved(mouseEvent -> emojiImage.setCursor(Cursor.HAND));
        fileImage.setOnMouseMoved(mouseEvent -> fileImage.setCursor(Cursor.HAND));
        emojiImage.setOnMouseClicked(mouseEvent -> showEmojiList(emojiImage,mouseEvent));
        fileImage.setOnMouseClicked(mouseEvent -> showFileChoose());

        Label nameLabel = initNameLabel(newChat);
        if (newChat[1].equals(ChatEnum.GROUP.code)){
            Label groupFunctionLabel = new Label("群功能");
            groupFunctionLabel.setCursor(Cursor.HAND);
            functionArea.getChildren().addAll(nameLabel,groupFunctionLabel,fileImage,emojiImage,button);
            HBox.setMargin(groupFunctionLabel,new Insets(0,200,0,0));
            HBox.setMargin(nameLabel,new Insets(0,150,0,0));

            groupFunctionLabel.setOnMouseClicked(mouseEvent -> showGroupFunctionMenu(groupFunctionLabel,mouseEvent));
        }else{
            functionArea.getChildren().addAll(nameLabel,fileImage,emojiImage,button);
            HBox.setMargin(nameLabel,new Insets(0,350,0,0));
        }
        functionArea.setAlignment(Pos.CENTER_RIGHT);
        functionArea.setSpacing(15);
        functionArea.setPrefWidth(borderPane.getPrefWidth());
        functionArea.setAlignment(Pos.CENTER_RIGHT);
        functionArea.setPadding(new Insets(0,20,0,0));
        borderPane.setBottom(functionArea);

        currentChatPane = borderPane;
        chatArea.setCenter(currentChatPane);
        borderPane.prefWidthProperty().bind(chatArea.widthProperty());
        borderPane.prefHeightProperty().bind(chatArea.heightProperty());

        Platform.runLater(()->{
            ScrollBar vScrollBar = (ScrollBar) chatHistoryScrollPane.lookup(".scroll-bar:vertical");
            vScrollBar.valueProperty().addListener(chatHistoryScrollListener);
        });

        queryChatHistory(newChat[1].equals(ChatEnum.FRIEND.code)?ChatEnum.FRIEND:ChatEnum.GROUP,
                (Long) newChat[0],1);
    }

    //初始化表情弹窗
    public void initEmojiList(){
        GridPane gridPane = new GridPane();
        gridPane.setPrefWidth(emojiScrollPaneWidth);
        gridPane.setPrefHeight(emojiScrollPaneHeight);
        final int[] i = {0};
        final int[] j = {0};
        final int[] index = {0};
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:/gui/img/emoji/*.png");
            for (Resource resource : resources) {
                int tmp = index[0];
                try (InputStream inputStream = resource.getInputStream()){
                    ImageView imageView = new ImageView(new Image(inputStream));
                    imageView.setFitHeight(emojiWidth);
                    imageView.setFitWidth(emojiWidth);
                    imageView.setOnMouseMoved(mouseEvent -> imageView.setCursor(Cursor.HAND));
                    imageView.setOnMouseClicked(mouseEvent -> insertEmoji(tmp));
                    gridPane.add(imageView, j[0], i[0]);
                    j[0]++;
                    index[0]++;
                    if (j[0] >= emojiScrollPaneWidth/emojiWidth){
                        i[0]++;
                        j[0] = j[0] % (int)(emojiScrollPaneWidth/emojiWidth);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            emojiScrollPane.setContent(gridPane);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //显示表情
    public void showEmojiList(ImageView emojiImage,MouseEvent mouseEvent){
        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        double x = emojiImage.localToScreen(mouseEvent.getX(), 0).getX();
        double y = emojiImage.localToScreen(0,mouseEvent.getY()).getY();
        Screen screen = Screen.getPrimary();
        if (x + emojiScrollPaneWidth >= screen.getVisualBounds().getWidth()){
            stage.setX(x - emojiScrollPaneWidth);
        }else{
            stage.setX(x);
        }
        if (y + emojiScrollPaneHeight >= screen.getVisualBounds().getHeight()){
            stage.setY(y - emojiScrollPaneHeight);
        }else{
            stage.setY(y);
        }
        stage.setScene(emojiScene);
        stage.show();
        emojiScrollPane.setOnMouseExited(mouseEvent1 -> stage.close());
    }

    //显示文件选择窗口
    public void showFileChoose(){
        Stage stage = new Stage();
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(stage);
        stage.close();
    }

    public void insertEmoji(int index){
        HTMLEditor inputArea = (HTMLEditor) currentChatPane.getCenter();
        Document doc = Jsoup.parse(inputArea.getHtmlText());
        Element img = doc.createElement("img");
        img.attr("src",sb.append(GUIConstant.IMAGE_BASE64_PREFIX)
                .append(GUIConstant.EMOJI_BASE_64.get(index)).toString());
        img.attr("height","20px");
        img.attr("width","20px");
        img.attr(GUIConstant.EMOJI_IMAGE_ATTR_KEY,String.valueOf(index));
        Element p = doc.body().getElementsByTag("p").first();
        if (p != null){
            p.appendChild(img);
        }else{
            Element newP = doc.createElement("p");
            newP.appendChild(img);
            doc.body().appendChild(newP);
        }
        inputArea.setHtmlText(doc.html());
        sb.delete(0,sb.length());
    }

    //发送聊天内容
    public void sendMsg(Long id, Integer chatEnumCode){
        HTMLEditor inputArea = (HTMLEditor) currentChatPane.getCenter();
        Map<String, MessageFilter> filters = applicationContext.getBeansOfType(MessageFilter.class);
        final String[] result = {inputArea.getHtmlText()};
        filters.forEach((name,filter)-> result[0] = filter.encode(result[0]));
        inputArea.setHtmlText("");
        if (chatEnumCode.equals(ChatEnum.FRIEND.code)){
            FriendChatData data = new FriendChatData(PPClientContext.token,PPClientContext.uid,id,result[0]);
            PPProtocol<FriendChatData> ppProtocol = PPProtocol.of(PPProtocolEnum.FRIEND_CHAT,data);
            afterSendMsg(netUtil.sendMessage(ppProtocol),result[0]);
        }else if (chatEnumCode.equals(ChatEnum.GROUP.code)){
            GroupChatData data = new GroupChatData(PPClientContext.token,PPClientContext.uid,id,result[0]);
            PPProtocol<GroupChatData> ppProtocol = PPProtocol.of(PPProtocolEnum.GROUP_CHAT,data);
            afterSendMsg(netUtil.sendMessage(ppProtocol),result[0]);
        }

        System.gc();
    }

    private void afterSendMsg(CompletableFuture<String> future, String msg){
        future.whenComplete((s, throwable) -> Platform.runLater(()->{
            ScrollPane scrollPane = (ScrollPane) currentChatPane.getTop();
            ScrollBar vScrollBar = (ScrollBar) scrollPane.lookup(".scroll-bar:vertical");
            insertChatBubble(false,null,msg);
            chatHistoryScrollListener.setScrollPane(vScrollBar);
            vScrollBar.setValue(1.0);
        })).exceptionally((e) -> {
            AlertGenerator.showError("网络异常，请稍后重试");
            return null;
        });
    }

    public void insertChatBubble(boolean isLeft, String avatar, String message){
        ScrollPane scrollPane = (ScrollPane) currentChatPane.getTop();
        TextFlow chatHistory = (TextFlow) scrollPane.getContent();
        insertChatBubble(isLeft, avatar, message, chatHistory.getChildren().size());
    }


    public void insertChatBubble(boolean isLeft, String avatar, String message, int pos) {
        HBox hbox = new HBox();
        hbox.prefWidthProperty().bind(chatArea.widthProperty().subtract(20));
        ImageView imageView;
        if (isLeft){
            hbox.setAlignment(Pos.TOP_LEFT);
            imageView = GUIConstant.initUserAvatar(avatar);

        }else{
            if (ObjectUtil.isEmpty(PPClientContext.avatar)){
                imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(GUIConstant.DEFAULT_USER_AVATAR_STRING))));
            }else{
                byte[] bytes = Base64.getDecoder().decode(PPClientContext.avatar);
                Image image = new Image(new ByteArrayInputStream(bytes));
                imageView = new ImageView(image);
            }
            hbox.setAlignment(Pos.TOP_RIGHT);
        }
        imageView.setFitWidth(25);
        imageView.setFitHeight(25);

        TextFlow bubble = new TextFlow();
        bubble.maxWidthProperty().bind(chatArea.widthProperty().multiply(0.6));
        bubble.setStyle("-fx-background-color: #5bfb6b");
        bubble.setBorder(new Border(new BorderStroke(Color.HOTPINK,
                BorderStrokeStyle.SOLID,
                new CornerRadii(0),
                new BorderWidths(1)
        )));

        Map<String, MessageFilter> filters = applicationContext.getBeansOfType(MessageFilter.class);
        String[] result = {message};
        filters.forEach((name,filter)-> result[0] = filter.decode(result[0]));
        Matcher matcher = GUIConstant.EMOJI_PATTERN.matcher(result[0]);

        //将字符串替换成表情图片
        int index = 0;
        while (matcher.find()){
            if (index != matcher.start()){
                Text text = new Text(result[0].substring(index,matcher.start()));
                text.setStyle("-fx-font-size: 17;-fx-text-alignment: center");
                bubble.getChildren().add(text);
            }
            String emojiStr = matcher.group();
            int emojiIndex = Integer.parseInt(emojiStr.substring(emojiStr.indexOf(":")+1,emojiStr.indexOf("]")));
            String emojiBase64 = GUIConstant.EMOJI_BASE_64.get(emojiIndex);
            byte[] emojiBytes = Base64.getDecoder().decode(emojiBase64);
            ImageView emojiImage = new ImageView(new Image(new ByteArrayInputStream(emojiBytes)));
            emojiImage.setFitHeight(25);
            emojiImage.setFitWidth(25);
            bubble.getChildren().add(emojiImage);
            index = matcher.end();
        }

        Text text = new Text(result[0].substring(index));
        text.setStyle("-fx-font-size: 17;-fx-text-alignment: center");
        bubble.getChildren().add(text);

        Polygon triangle = new Polygon();
        if (isLeft){
            triangle.getPoints().addAll(10.0, 0.0,
                    0.0, 5.0,
                    10.0, 10.0);
            hbox.getChildren().addAll(imageView, triangle, bubble);
        }else{
            triangle.getPoints().addAll(0.0, 0.0,
                    10.0, 5.0,
                    0.0, 10.0);
            hbox.getChildren().addAll(bubble,triangle, imageView);
        }
        triangle.setFill(Color.LIGHTGREEN); // 设置填充颜色
        ScrollPane scrollPane = (ScrollPane) currentChatPane.getTop();
        TextFlow chatHistory = (TextFlow) scrollPane.getContent();
        pos = Math.max(pos, 0);
        chatHistory.getChildren().add(pos,hbox);
    }

    private void queryChatHistory(ChatEnum chatEnum,Long id,int page){
        ScrollPane scrollPane = (ScrollPane) chatPaneMap.get(id).getTop();
        TextFlow chatHistory = (TextFlow) scrollPane.getContent();
        if (chatHistory.getChildren().size()>0){
            chatHistory.getChildren().remove(0);
        }
        if (chatEnum == ChatEnum.FRIEND){
            QueryFriendChatRequestData data = new QueryFriendChatRequestData(PPClientContext.token,id,page, BaseConstant.DEFAULT_FRIEND_CHAT_LIMIT);
            PPProtocol<QueryFriendChatRequestData> ppProtocol = PPProtocol.of(PPProtocolEnum.QUERY_FRIEND_CHAT_REQUEST,data);
            netUtil.sendMessage(ppProtocol);
        }else if (chatEnum == ChatEnum.GROUP){
            QueryGroupChatRequestData data = new QueryGroupChatRequestData(PPClientContext.token,id,BaseConstant.DEFAULT_GROUP_CHAT_LIMIT,page);
            PPProtocol<QueryGroupChatRequestData> ppProtocol = PPProtocol.of(PPProtocolEnum.QUERY_GROUP_CHAT_REQUEST,data);
            netUtil.sendMessage(ppProtocol);
        }
    }

    public void addHistoryLabel(Long id,ChatEnum chatEnum){
        if (!chatPaneMap.containsKey(id)) return;
        HBox hBox = new HBox();
        hBox.prefWidthProperty().bind(chatArea.widthProperty().subtract(20));
        Label label = new Label("查看历史记录");
        label.setPrefWidth(100);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-background-color: #d1d1d1");
        label.setCursor(Cursor.HAND);

        label.setOnMouseClicked(mouseEvent -> {
            if (chatEnum == ChatEnum.FRIEND){
                queryChatHistory(chatEnum,id,PPClientContext.friendChatPages.get(id).getNextPage());
            }else if (chatEnum == ChatEnum.GROUP){
                queryChatHistory(chatEnum,id,PPClientContext.groupChatPages.get(id).getNextPage());
            }

        });

        hBox.getChildren().add(label);
        hBox.setAlignment(Pos.CENTER);
        ScrollPane scrollPane = (ScrollPane) chatPaneMap.get(id).getTop();
        TextFlow chatHistory = (TextFlow) scrollPane.getContent();
        chatHistory.getChildren().add(0,hBox);
    }

    public void addTimeLabel(Long id,Long time,int index){
        if (!chatPaneMap.containsKey(id)) return;
        HBox hBox = new HBox();
        hBox.prefWidthProperty().bind(chatArea.widthProperty().subtract(20));
        Label label = new Label();
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = sdf.format(date);
        label.setText(format);
        label.setPrefWidth(150);
        label.setAlignment(Pos.CENTER);
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().add(label);
        ScrollPane scrollPane = (ScrollPane) chatPaneMap.get(id).getTop();
        TextFlow chatHistory = (TextFlow) scrollPane.getContent();
        chatHistory.getChildren().add(index,hBox);
    }

    private Long getGroupIdByCurrentChatPane(){
        AtomicReference<Long> groupId = new AtomicReference<>();
        chatPaneMap.forEach((k,v)->{
            if (v == currentChatPane){
                groupId.set(k);
            }
        });
        return groupId.get();
    }

    private void initGroupFunctionMenu(){
        groupFunctionMenu.getItems().addAll(showMember,invite);
        showMember.setOnAction(actionEvent -> {
            Long groupId = getGroupIdByCurrentChatPane();
            if (ObjectUtil.isEmpty(groupId)) return;
            initGroupMemberList(groupId);
        });
    }

    private void initMemberFunctionMenu(){
        memberFunctionMenu.getItems().addAll(levelUp,levelDown,silence,kick);
        levelUp.setOnAction(actionEvent -> {
            Long groupId = getGroupIdByCurrentChatPane();
            if (ObjectUtil.isEmpty(groupId)) return;
            levelUpdateMember(GroupMemberLevel.MANAGER);
        });
        levelDown.setOnAction(actionEvent -> {
            Long groupId = getGroupIdByCurrentChatPane();
            if (ObjectUtil.isEmpty(groupId)) return;
            levelUpdateMember(GroupMemberLevel.MEMBER);
        });
        kick.setOnAction(actionEvent -> {
            Long groupId = getGroupIdByCurrentChatPane();
            if (ObjectUtil.isEmpty(groupId)) return;
            kickMember();
        });
    }

    private void initGroupMemberList(Long groupId){
        Stage stage = new Stage();
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefWidth(300);
        Scene scene = new Scene(scrollPane);
        TableView<GroupMember> tableView = new TableView<>();
        currentMemberTable = tableView;
        Optional<ObservableList<GroupMember>> optionalGroupMembers = PPClientContext.groups.stream()
                .filter(group -> group.getId().equals(groupId))
                .map(group -> FXCollections.observableArrayList(group.getMembers()))
                .findFirst();
        optionalGroupMembers.ifPresent(groupMembers -> {
            currentMemberList = groupMembers;
            tableView.setItems(groupMembers);
        });
        scrollPane.setContent(tableView);
        tableView.prefWidthProperty().bind(scrollPane.widthProperty().subtract(15));
        TableColumn<GroupMember,GroupMember> tableColumn = new TableColumn<>();
        tableColumn.setCellValueFactory((param)-> new SimpleObjectProperty<>(param.getValue()));
        tableColumn.setCellFactory(memberTableColumn-> new TableCell<>(){
            @Override
            protected void updateItem(GroupMember groupMember,boolean empty){
                super.updateItem(groupMember,empty);
                if (ObjectUtil.isNotEmpty(groupMember) && !empty){
                    HBox hBox = new HBox();
                    ImageView avatar = GUIConstant.initUserAvatar(groupMember.getAvatar());
                    avatar.setFitHeight(50);
                    avatar.setFitWidth(50);
                    Label nameLabel = new Label(groupMember.getName());
                    Label levelLabel = new Label(groupMember.getLevelDescription());
                    hBox.getChildren().addAll(avatar,nameLabel,levelLabel);
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    HBox.setMargin(nameLabel,new Insets(0,0,0,20));
                    HBox.setMargin(levelLabel,new Insets(0,0,0,20));
                    this.setGraphic(hBox);
                }else{
                    this.setGraphic(null);
                }
            }
        });

        tableColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(5));
        tableView.getColumns().add(tableColumn);
        tableView.setRowFactory((tv) -> {
            TableRow<GroupMember> row = new TableRow<>();
            row.setMinHeight(50);
            row.setMaxHeight(50);
            return row;
        });
        tableView.setOnMouseClicked(mouseEvent -> {
            int index = tableView.getSelectionModel().getSelectedIndex();
            GroupMember member = tableView.getItems().get(index);
            currentMemberId = member.getId();
            if (mouseEvent.getClickCount() == 1){
                memberFunctionMenu.hide();
            }
            if (mouseEvent.getClickCount() == 2 || mouseEvent.getButton() == MouseButton.SECONDARY){
                double screenX = tableView.localToScreen(mouseEvent.getX(), 0).getX();
                double screenY = tableView.localToScreen(0, mouseEvent.getY()).getY();

                memberFunctionMenu.show(tableView, screenX, screenY);
            }
        });
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(windowEvent -> {
            currentMemberList = null;
            currentMemberTable = null;
        });
    }

    private void showGroupFunctionMenu(Label label,MouseEvent mouseEvent){
        double screenX = label.localToScreen(mouseEvent.getX(), 0).getX();
        double screenY = label.localToScreen(0, mouseEvent.getY()).getY();
        groupFunctionMenu.show(label,screenX,screenY);
    }

    private void levelUpdateMember(GroupMemberLevel groupMemberLevel){
        Optional<ButtonType> buttonType = AlertGenerator.showConfirm("你确定要这么做吗?");
        if (buttonType.isEmpty() || buttonType.get() == ButtonType.CANCEL) return;
        if (currentMemberId.equals(PPClientContext.uid)){
            AlertGenerator.showError("无法对自己进行该操作");
            return;
        }
        LevelUpdateMemberData data = new LevelUpdateMemberData(PPClientContext.token,getGroupIdByCurrentChatPane(),
                currentMemberId, groupMemberLevel);
        PPProtocol<LevelUpdateMemberData> ppProtocol = PPProtocol.of(PPProtocolEnum.LEVEL_UPDATE_MEMBER,data);
        CompletableFuture<String> future = netUtil.sendMessage(ppProtocol);
        future.whenComplete((res,throwable)->{
            if (ObjectUtil.isEmpty(currentMemberList)) return;
            Optional<GroupMember> optionalGroupMember = currentMemberList.stream()
                    .filter(groupMember -> groupMember.getId().equals(currentMemberId))
                    .findFirst();
            optionalGroupMember.ifPresent(groupMember -> {
                groupMember.setLevel(groupMemberLevel.level);
                groupMember.setLevelDescription(groupMemberLevel.levelDescription);
                currentMemberTable.refresh();
            });
        });
    }

    private void kickMember(){
        Optional<ButtonType> buttonType = AlertGenerator.showConfirm("你确定要这么做吗?");
        if (buttonType.isEmpty() || buttonType.get() == ButtonType.CANCEL) return;
        if (currentMemberId.equals(PPClientContext.uid)){
            AlertGenerator.showError("无法对自己进行该操作");
            return;
        }
        KickMemberData data = new KickMemberData(PPClientContext.token,getGroupIdByCurrentChatPane(),currentMemberId);
        PPProtocol<KickMemberData> ppProtocol = PPProtocol.of(PPProtocolEnum.KICK_MEMBER,data);
        CompletableFuture<String> future = netUtil.sendMessage(ppProtocol);
        future.whenComplete((res,throwable)->{
            if (ObjectUtil.isEmpty(currentMemberList)) return;
            Optional<GroupMember> optionalGroupMember = currentMemberList.stream()
                    .filter(groupMember -> groupMember.getId().equals(currentMemberId))
                    .findFirst();
            optionalGroupMember.ifPresent(groupMember -> {
                currentMemberList.remove(groupMember);
                currentMemberTable.refresh();
            });
        });
    }


    private static class ChatHistoryScrollListener implements ChangeListener<Number>{

        ThreadLocal<ScrollBar> scrollBar = new ThreadLocal<>();

        public void setScrollPane(ScrollBar scrollBar){
            this.scrollBar.set(scrollBar);
        }

        @Override
        public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
            if (ObjectUtil.isNotEmpty(scrollBar.get())){
                scrollBar.get().setValue(1.0);
                scrollBar.remove();
            }
        }
    }
}
