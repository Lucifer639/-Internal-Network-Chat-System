package com.lucifer.pp.client.function;

import cn.hutool.json.JSONObject;
import com.lucifer.pp.ClientApplication;
import com.lucifer.pp.gui.controller.LoginPaneController;
import com.lucifer.pp.gui.util.AlertGenerator;
import com.lucifer.pp.gui.view.LoginView;
import com.lucifer.pp.net.data.PPMessage;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import com.lucifer.pp.net.netenum.StatusEnum;
import jakarta.annotation.Resource;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.springframework.stereotype.Component;


@Component
public class MessageFunction implements PPFunction {

    private static final PPProtocolEnum protocol = PPProtocolEnum.MESSAGE;

    @Override
    public Object apply(Object o) {
        PPMessage data = ((JSONObject) o).toBean(PPMessage.class);
        Platform.runLater(()->{
            if (data.getStatusEnum() == StatusEnum.ERROR)
                AlertGenerator.showError(data.getMessage());
            else
                AlertGenerator.showInfo(data.getMessage());
            if (data.getOriginProtocol() == PPProtocolEnum.LOGIN_REQUEST || data.getOriginProtocol() == PPProtocolEnum.REGISTER_REQUEST){
                ClientApplication.showView(LoginView.class);
            }
        });
        return "ok";
    }

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }
}
