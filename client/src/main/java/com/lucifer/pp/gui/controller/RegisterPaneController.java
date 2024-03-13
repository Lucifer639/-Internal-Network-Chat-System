package com.lucifer.pp.gui.controller;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.lucifer.pp.client.util.NetUtil;
import com.lucifer.pp.common.properties.ServerProperties;
import com.lucifer.pp.common.security.SHA256;
import com.lucifer.pp.ClientApplication;
import com.lucifer.pp.gui.util.AlertGenerator;
import com.lucifer.pp.gui.view.LoginView;
import com.lucifer.pp.gui.view.RegisteringView;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.data.RegisterRequestData;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import de.felixroske.jfxsupport.FXMLController;
import jakarta.annotation.Resource;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import lombok.Data;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;


@FXMLController
@Data
public class RegisterPaneController implements Initializable {
    @FXML
    private TextField serverIP;
    @FXML
    private TextField userCode;
    @FXML
    private TextField name;
    @FXML
    private TextField password;
    @FXML
    private TextField confirm;
    @FXML
    private Button register;
    @FXML
    private Button cancel;
    @FXML
    private BorderPane registerPane;

    private Scene scene;
    private BorderPane loginPane;
    private BorderPane registeringPane;

    private SimpleStringProperty userCodeText = new SimpleStringProperty();
    private SimpleStringProperty nameText = new SimpleStringProperty();
    private SimpleStringProperty pwd = new SimpleStringProperty();
    private SimpleStringProperty pwdConfirm = new SimpleStringProperty();

    @Resource
    NetUtil netUtil;

    @Resource
    ServerProperties serverProperties;

    @FXML
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){
        serverIP.setText(serverProperties.getIp());
        Platform.runLater(()->name.requestFocus());
        serverIP.textProperty().addListener((observableValue, s, t1) -> serverProperties.setIp(t1));
        userCode.textProperty().bindBidirectional(userCodeText);
        name.textProperty().bindBidirectional(nameText);
        password.textProperty().bindBidirectional(pwd);
        confirm.textProperty().bindBidirectional(pwdConfirm);
    }

    @FXML
    public void cancel(){
        ClientApplication.showView(LoginView.class);
    }

    @FXML
    public void register(){
        if (ObjectUtil.hasEmpty(nameText.get(),pwd.get(),pwdConfirm.get())){
            AlertGenerator.showError("信息不得为空!");
        }else if (!pwd.get().equals(pwdConfirm.get())){
            AlertGenerator.showError("两次密码不一致!");
        }else if (!Validator.isMobile(userCodeText.get())){
            AlertGenerator.showError("手机号校验失败!");
        }else{
            ClientApplication.showView(RegisteringView.class);
            RegisterRequestData data = new RegisterRequestData(userCodeText.get(),nameText.get(), SHA256.encode(pwd.get()));
            PPProtocol<RegisterRequestData> ppProtocol = PPProtocol.of(PPProtocolEnum.REGISTER_REQUEST,data);
            netUtil.sendMessage(ppProtocol);
        }
    }

}
