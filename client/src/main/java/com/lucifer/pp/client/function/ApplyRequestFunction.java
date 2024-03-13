package com.lucifer.pp.client.function;

import cn.hutool.json.JSONObject;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.common.dto.Application;
import com.lucifer.pp.gui.constant.GUIConstant;
import com.lucifer.pp.gui.controller.ClientPaneController;
import com.lucifer.pp.gui.controller.NoticePaneController;
import com.lucifer.pp.net.context.ChannelContext;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ApplyRequestFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.APPLY_REQUEST;
    private final ClientPaneController clientPaneController;
    private final NoticePaneController noticePaneController;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    public Object apply(Object o) {
        Application application = ((JSONObject) o).toBean(Application.class);
        if (!PPClientContext.noticeWindowExist){
            Platform.runLater(()->{
                ImageView imageView = GUIConstant.NOTICE_NEW_IMAGE;
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                clientPaneController.noticeLabel.setGraphic(imageView);
            });
        }else{
            PPClientContext.applications.add(application);
            noticePaneController.noticeTable.refresh();
        }
        return ChannelContext.release();
    }
}
