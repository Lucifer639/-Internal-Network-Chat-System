package com.lucifer.pp.gui.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.lucifer.pp.client.PPClient;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.client.util.NetUtil;
import com.lucifer.pp.ClientApplication;
import com.lucifer.pp.common.base.BaseConstant;
import com.lucifer.pp.common.properties.ServerProperties;
import com.lucifer.pp.gui.view.LoginingView;
import com.lucifer.pp.gui.view.RegisterView;
import com.lucifer.pp.net.data.LoginRequestData;
import com.lucifer.pp.net.data.PPProtocol;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.common.security.SHA256;
import com.lucifer.pp.common.service.sys.SysUserService;
import de.felixroske.jfxsupport.FXMLController;
import jakarta.annotation.Resource;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import lombok.Data;

import java.io.*;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

@FXMLController
@Data
public class LoginPaneController implements Initializable {

    @FXML
    private TextField serverIP;
    @FXML
    private TextField uid;
    @FXML
    private PasswordField password;
    @FXML
    private CheckBox remember;
    @FXML
    private CheckBox auto;
    @FXML
    private Button register;
    @FXML
    private Button login;

    private Scene scene;

    @Resource
    SysUserService userService;

    @FXML
    private BorderPane loginPane;

    private BorderPane loginingPane;
    private BorderPane registerPane;

    public SimpleStringProperty uidText = new SimpleStringProperty();
    public SimpleStringProperty pwd = new SimpleStringProperty();
    public SimpleBooleanProperty isRemember = new SimpleBooleanProperty();
    public SimpleBooleanProperty isAuto = new SimpleBooleanProperty();

    @Resource
    NetUtil netUtil;

    @Resource
    PPClient ppClient;

    @Resource
    ServerProperties serverProperties;

    @Override
    @FXML
    public void initialize(URL url, ResourceBundle resourceBundle){
        serverIP.setText(serverProperties.getIp());
        Platform.runLater(()->uid.requestFocus());
        serverIP.textProperty().addListener((observableValue, s, t1) -> serverProperties.setIp(t1));
        uid.textProperty().bindBidirectional(uidText);
        password.textProperty().bindBidirectional(pwd);
        remember.selectedProperty().bindBidirectional(isRemember);
        auto.selectedProperty().bindBidirectional(isAuto);
        ClientApplication.getStage().setOnCloseRequest(windowEvent -> {
            ppClient.future.channel().close();
            if (ObjectUtil.isNotEmpty(PPClientContext.heartBeatFuture)){
                PPClientContext.heartBeatFuture.cancel(false);
            }
            PPClientContext.heartBeatExecutor.shutdown();
            Platform.exit();
        });

        password.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER){
                login.fire();
            }
        });

        readConfig();
    }

    @FXML
    public void login() throws Exception {
        if (ObjectUtil.isNotEmpty(uidText.get()) && ObjectUtil.isNotEmpty(pwd.get()) && ObjectUtil.isNotEmpty(serverIP.getText())){
            ClientApplication.showView(LoginingView.class);
            LoginRequestData loginData = new LoginRequestData(uidText.get(), SHA256.encode(pwd.get()),
                    InetAddress.getLocalHost().getHostAddress(),isRemember.get(),isAuto.get());
            PPProtocol<LoginRequestData> ppProtocol = PPProtocol.of(PPProtocolEnum.LOGIN_REQUEST,loginData);
            netUtil.sendMessage(ppProtocol);
            InputStream ins = new FileInputStream(BaseConstant.USER_CONFIG_PATH+"/"+BaseConstant.USER_CONFIG_FILE_NAME);
            OutputStream ops = new FileOutputStream(BaseConstant.USER_CONFIG_PATH+"/"+BaseConstant.USER_CONFIG_FILE_NAME,true);
            Properties prop = new Properties();
            prop.load(ins);
            prop.setProperty("remember",String.valueOf(remember.isSelected()));
            prop.setProperty("auto",String.valueOf(auto.isSelected()));
            if (remember.isSelected() || auto.isSelected()){
                prop.setProperty("uid",uidText.get());
                prop.setProperty("password",pwd.get());
            }
            prop.store(ops,"");
            ops.close();
            ins.close();
        }
    }

    //读取用户配置文件
    @SuppressWarnings("all")
    private void readConfig(){
        try {
            Properties prop= new Properties();
            InputStream ins = null;
            File file = new File(BaseConstant.USER_CONFIG_PATH,BaseConstant.USER_CONFIG_FILE_NAME);
            if (!file.exists()){
                new File(BaseConstant.USER_CONFIG_PATH).mkdirs();
                file.createNewFile();
            }else{
                ins = new FileInputStream(BaseConstant.USER_CONFIG_PATH+"/"+BaseConstant.USER_CONFIG_FILE_NAME);
                prop.load(ins);
                if (ObjectUtil.isNotEmpty(prop.getProperty("remember")) && Boolean.parseBoolean(prop.getProperty("remember"))){
                    remember.setSelected(Boolean.valueOf(prop.getProperty("remember")));
                    remember(prop);
                }
                if (ObjectUtil.isNotEmpty(prop.getProperty("auto")) && Boolean.parseBoolean(prop.getProperty("auto"))){
                    auto.setSelected(Boolean.valueOf(prop.getProperty("auto")));
                    auto(prop);
                }
            }
            if (ObjectUtil.isNotEmpty(ins)){
                ins.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void remember(Properties prop){
        uid.setText(prop.getProperty("uid"));
        password.setText(prop.getProperty("password"));
    }

    private void auto(Properties prop){
        remember(prop);
        login.fire();
    }

    @FXML
    public void register(){
        ClientApplication.showView(RegisterView.class);
    }

}
