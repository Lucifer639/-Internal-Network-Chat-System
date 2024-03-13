package com.lucifer.pp.gui.controller;

import cn.hutool.core.util.ObjectUtil;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.client.util.NetUtil;
import com.lucifer.pp.gui.constant.GUIConstant;
import com.lucifer.pp.gui.util.AlertGenerator;
import com.lucifer.pp.net.data.ApplyRequestData;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.data.SearchRequestData;
import com.lucifer.pp.net.data.SearchResponseData;
import com.lucifer.pp.net.netenum.ApplyType;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.net.netenum.SearchType;
import de.felixroske.jfxsupport.FXMLController;
import jakarta.annotation.Resource;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Objects;
import java.util.ResourceBundle;

@FXMLController
public class SearchPaneController implements Initializable {

    @FXML
    public TextField searchWord;

    @FXML
    public RadioButton friendRadio;

    @FXML
    public RadioButton groupRadio;

    @FXML
    public TableView<SearchResponseData> searchTable;

    @FXML
    public Button searchButton;

    SearchType searchType;

    SearchType lastSearchType;

    @Resource
    NetUtil netUtil;

    private final ContextMenu applicantMenu = new ContextMenu();
    private final MenuItem applicantMenuItem = new MenuItem("申请加好友/入群");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        PPClientContext.searchData = FXCollections.observableArrayList();
        searchTable.setItems(PPClientContext.searchData);
        ToggleGroup searchToggle = new ToggleGroup();
        friendRadio.setToggleGroup(searchToggle);
        groupRadio.setToggleGroup(searchToggle);
        searchType = SearchType.FRIEND;
        searchToggle.selectedToggleProperty().addListener((observableValue, old_toggle, new_toggle) -> {
            RadioButton rb = (RadioButton) new_toggle;
            if (rb.getText().equals("好友")) searchType = SearchType.FRIEND;
            else searchType = SearchType.GROUP;
        });

        searchButton.setOnAction(actionEvent -> {
            search();
        });

        searchTable.setItems(PPClientContext.searchData);
        TableColumn<SearchResponseData,SearchResponseData> searchColumn = new TableColumn<>();
        searchColumn.setCellValueFactory(param-> new SimpleObjectProperty<>(param.getValue()));
        searchColumn.setCellFactory(searchResponseColumn -> new TableCell<>(){
            @Override
            protected void updateItem(SearchResponseData data,boolean empty){
                super.updateItem(data,empty);
                if (ObjectUtil.isNotEmpty(data) && !empty){
                    BorderPane borderPane = new BorderPane();
                    ImageView imageView;
                    if (ObjectUtil.isNotEmpty(data.getAvatar())){
                        byte[] imageBytes = Base64.getDecoder().decode(data.getAvatar());
                        imageView = new ImageView(new Image(new ByteArrayInputStream(imageBytes)));

                    }else{
                        if (lastSearchType == SearchType.FRIEND){
                            imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(GUIConstant.DEFAULT_USER_AVATAR_STRING))));
                        }else{
                            imageView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(GUIConstant.DEFAULT_GROUP_AVATAR_STRING))));
                        }
                    }
                    imageView.setFitWidth(80);
                    imageView.setFitHeight(80);
                    borderPane.setLeft(imageView);

                    Label nameLabel = new Label(data.getName());
                    borderPane.setCenter(nameLabel);
                    if (lastSearchType == SearchType.FRIEND){
                        Label onLineLabel = new Label(data.isOnline()?"在线":"离线");
                        borderPane.setRight(onLineLabel);
                    }else{
                        Label managerLabel = new Label("群主手机号:"+data.getUserCode());
                        borderPane.setRight(managerLabel);
                    }

                    this.setGraphic(borderPane);
                }else{
                    this.setGraphic(null);
                }
            }
        });
        searchColumn.prefWidthProperty().bind(searchTable.widthProperty());
        searchTable.getColumns().add(searchColumn);
        searchTable.setRowFactory((tv)->{
            TableRow<SearchResponseData> row = new TableRow<>();
            row.setMinHeight(80);
            row.setMaxHeight(80);
            return row;
        });

        initMenu();
    }

    private void search(){
        if (ObjectUtil.isEmpty(searchWord.getText())){
            AlertGenerator.showError("搜索字段不能为空");
            return;
        }
        lastSearchType = searchType;
        PPProtocol<SearchRequestData> protocol = new PPProtocol<>();
        protocol.setPpProtocol(PPProtocolEnum.SEARCH_REQUEST);
        SearchRequestData searchRequestData = new SearchRequestData(lastSearchType,searchWord.getText(),PPClientContext.token);
        protocol.setData(searchRequestData);
        netUtil.sendMessage(protocol);
    }

    private void initMenu(){
        applicantMenu.getItems().addAll(applicantMenuItem);
        searchTable.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 1){
                applicantMenu.hide();
            }
            if (mouseEvent.getButton() == MouseButton.SECONDARY){
                double screenX = searchTable.localToScreen(mouseEvent.getX(), 0).getX();
                double screenY = searchTable.localToScreen(0,mouseEvent.getY()).getY();
                applicantMenu.show(searchTable,screenX,screenY);
            }
        });

        applicantMenuItem.setOnAction(actionEvent -> apply());
    }

    private void apply(){
        PPProtocol<ApplyRequestData> ppProtocol = new PPProtocol<>();
        ppProtocol.setPpProtocol(PPProtocolEnum.APPLY_REQUEST);
        ApplyRequestData applyRequestData = new ApplyRequestData();
        int index = searchTable.getSelectionModel().getSelectedIndex();
        Long receiveId = PPClientContext.searchData.get(index).getId();
        if (lastSearchType == SearchType.FRIEND){

            if (PPClientContext.uid.equals(receiveId)){
                AlertGenerator.showError("你不能加你自己为好友!");
                return;
            }else{
                long count = PPClientContext.friends.stream()
                        .filter(friend -> friend.getId().equals(receiveId))
                        .count();
                if (count > 0){
                    AlertGenerator.showError("你们已经是好友!");
                    return;
                }
            }

            applyRequestData.setType(ApplyType.APPLY_FRIEND.code);
        }else{
            long count = PPClientContext.groups.stream()
                    .filter(group -> group.getId().equals(receiveId))
                    .count();
            if (count > 0){
                AlertGenerator.showError("你已在该群中!");
                return;
            }

            applyRequestData.setType(ApplyType.APPLY_GROUP.code);
            applyRequestData.setGroupName(PPClientContext.searchData.get(index).getName());
        }
        applyRequestData.setApplicantId(PPClientContext.uid);
        applyRequestData.setReceiveId(receiveId);
        applyRequestData.setUserName(PPClientContext.name);
        applyRequestData.setToken(PPClientContext.token);
        ppProtocol.setData(applyRequestData);
        netUtil.sendMessage(ppProtocol);
        AlertGenerator.showInfo("申请成功");
    }
}
