package com.lucifer.pp.gui.controller;

import cn.hutool.core.util.ObjectUtil;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.client.util.NetUtil;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.dto.Application;
import com.lucifer.pp.gui.constant.GUIConstant;
import com.lucifer.pp.net.data.ApplyResponseData;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.data.QueryApplyRequestData;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import de.felixroske.jfxsupport.FXMLController;
import jakarta.annotation.Resource;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Base64;
import java.util.Objects;
import java.util.ResourceBundle;

@FXMLController
public class NoticePaneController implements Initializable {

    @FXML
    public TableView<Application> noticeTable;
    @Resource
    NetUtil netUtil;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        noticeTable.setItems(PPClientContext.applications);
        TableColumn<Application,Application> tableColumn = new TableColumn<>();
        tableColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        tableColumn.setCellFactory(noticeTableColumn -> new TableCell<>(){
            @Override
            protected void updateItem(Application application,boolean empty){
                super.updateItem(application,empty);
                if (ObjectUtil.isNotEmpty(application) && !empty){
                    HBox hBox = new HBox();
                    ImageView avatar;
                    if (ObjectUtil.isNotEmpty(application.getAvatar())){
                        byte[] imageBytes = Base64.getDecoder().decode(application.getAvatar());
                        avatar = new ImageView(new Image(new ByteArrayInputStream(imageBytes)));
                    }else{
                        if (application.getType().equals(0) || application.getType().equals(1)){
                            avatar = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(GUIConstant.DEFAULT_USER_AVATAR_STRING))));
                        }else{
                            avatar = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(GUIConstant.DEFAULT_GROUP_AVATAR_STRING))));
                        }
                    }
                    avatar.setFitHeight(80);
                    avatar.setFitWidth(80);

                    Label description = new Label();
                    description.setTextAlignment(TextAlignment.CENTER);
                    if (application.getType().equals(0)){
                        description.setText(String.format("%s,申请加为好友",application.getUserName()));
                    }else if (application.getType().equals(1)){
                        description.setText(String.format("%s,申请加入群聊 %s",application.getUserName(),application.getGroupName()));
                    }else{
                        description.setText(String.format("%s,邀请你加入群聊 %s",application.getUserName(),application.getGroupName()));
                    }
                    description.setPrefHeight(80);

                    HBox hBox2 = new HBox();
                    hBox2.setAlignment(Pos.CENTER_LEFT);
                    if (ObjectUtil.isEmpty(application.getAgree())){
                        Button accept = new Button("同意");
                        Button refuse = new Button("拒绝");
                        accept.setOnAction(actionEvent -> accept(application));
                        refuse.setOnAction(actionEvent -> refuse(application));
                        hBox2.getChildren().addAll(accept,refuse);
                    }else{
                        Label label = new Label(application.getAgree().equals(0)?"已拒绝":"已同意");
                        hBox2.getChildren().add(label);
                    }


                    hBox.getChildren().addAll(avatar,description,hBox2);
                    hBox.alignmentProperty().set(Pos.CENTER_LEFT);
                    hBox.spacingProperty().set(10);
                    this.setGraphic(hBox);
                }else{
                    this.setGraphic(null);
                }
            }
        });

        tableColumn.prefWidthProperty().bind(noticeTable.widthProperty());
        noticeTable.getColumns().add(tableColumn);
        noticeTable.setRowFactory((tv) -> {
            TableRow<Application> row = new TableRow<>();
            row.setMaxHeight(80);
            row.setMinHeight(80);
            return row;
        });

        Platform.runLater(()->{
            ScrollBar vBar = (ScrollBar) noticeTable.lookup(".scroll-bar:vertical");
            vBar.valueProperty().addListener((observableValue, number, t1) -> {
                if (t1.floatValue() >= 1 && PPClientContext.applicationPageInfo.isHasNextPage()){
                    PPProtocol<QueryApplyRequestData> ppProtocol = new PPProtocol<>();
                    ppProtocol.setPpProtocol(PPProtocolEnum.QUERY_APPLY_REQUEST);
                    QueryApplyRequestData data = new QueryApplyRequestData(PPClientContext.token, PPClientContext.applicationPageInfo.getNextPage(),
                            BaseConstant.DEFAULT_APPLICATION_LIMIT);
                    ppProtocol.setData(data);
                    netUtil.sendMessage(ppProtocol);
                }
            });
        });

    }

    private void accept(Application application){
        ApplyResponseData data = new ApplyResponseData();
        data.setToken(PPClientContext.token);
        application.setAgree(1);
        noticeTable.refresh();
        data.setAgree(true);
        data.setApplicantId(application.getApplicantId());
        data.setReceiveId(application.getReceiveId());
        data.setType(application.getType());
        PPProtocol<ApplyResponseData> ppProtocol = new PPProtocol<>(PPProtocolEnum.APPLY_RESPONSE,data);
        netUtil.sendMessage(ppProtocol);
    }
    private void refuse(Application application){
        ApplyResponseData data = new ApplyResponseData();
        data.setToken(PPClientContext.token);
        application.setAgree(0);
        noticeTable.refresh();
        data.setAgree(false);
        data.setApplicantId(application.getApplicantId());
        data.setReceiveId(application.getReceiveId());
        data.setType(application.getType());
        PPProtocol<ApplyResponseData> ppProtocol = new PPProtocol<>(PPProtocolEnum.APPLY_RESPONSE,data);
        netUtil.sendMessage(ppProtocol);
    }
}
