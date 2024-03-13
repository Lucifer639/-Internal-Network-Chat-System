package com.lucifer.pp.client.function;

import cn.hutool.json.JSONObject;
import com.lucifer.pp.ClientApplication;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.common.security.TokenUtil;
import com.lucifer.pp.gui.view.ClientView;
import com.lucifer.pp.net.data.LoginResponseData;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import org.springframework.stereotype.Component;

@Component
public class LoginResponseFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.LOGIN_RESPONSE;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    public Object apply(Object o) {
        LoginResponseData data = ((JSONObject) o).toBean(LoginResponseData.class);
        PPClientContext.token = data.getToken();
        PPClientContext.uid = TokenUtil.getUID(data.getToken());
        PPClientContext.name = data.getName();
        PPClientContext.userCode = data.getUserCode();
        PPClientContext.friends = FXCollections.observableArrayList();
        PPClientContext.friends.addAll(data.getFriends());
        PPClientContext.groups = FXCollections.observableArrayList();
        PPClientContext.groups.addAll(data.getGroups());
        PPClientContext.applications = FXCollections.observableArrayList();
        PPClientContext.newChats = FXCollections.observableArrayList();
//        PPClientContext.applications.addAll(data.getApplications());
        Platform.runLater(()->{
            ClientApplication.getStage().close();
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            ClientApplication.showView(ClientView.class);
            ClientApplication.getStage().setX(bounds.getWidth()-400);
            ClientApplication.getStage().setY(0);
        });
        return "ok";
    }
}
