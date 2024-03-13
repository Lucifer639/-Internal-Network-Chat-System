package com.lucifer.pp.client.function;

import com.lucifer.pp.ClientApplication;
import com.lucifer.pp.gui.view.ClientView;
import com.lucifer.pp.gui.view.LoginView;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import org.springframework.stereotype.Component;

@Component
public class ReLoginFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.RE_LOGIN;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    public Object apply(Object o) {
        Platform.runLater(()->{
            ClientApplication.getStage().close();
            ClientApplication.showView(LoginView.class);
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            ClientApplication.getStage().setX(bounds.getWidth()/2-250);
            ClientApplication.getStage().setY(bounds.getHeight()/2-150);
        });
        return ChannelContext.release();
    }
}
