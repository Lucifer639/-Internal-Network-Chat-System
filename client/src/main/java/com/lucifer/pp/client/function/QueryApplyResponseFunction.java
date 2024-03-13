package com.lucifer.pp.client.function;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.github.pagehelper.PageInfo;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.common.dto.Application;
import com.lucifer.pp.gui.controller.NoticePaneController;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import javafx.collections.FXCollections;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class QueryApplyResponseFunction implements PPFunction{

    private static final PPProtocolEnum protocol = PPProtocolEnum.QUERY_APPLY_RESPONSE;
    private final NoticePaneController noticePaneController;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    public Object apply(Object o) {
        if (ObjectUtil.isEmpty(PPClientContext.applications)){
            PPClientContext.applications = FXCollections.observableArrayList();
            noticePaneController.noticeTable.setItems(PPClientContext.applications);
        }
        PageInfo pageInfo = ((JSONObject) o).toBean(PageInfo.class);
        PPClientContext.applicationPageInfo = pageInfo;
        List<JSONObject> list = pageInfo.getList();
        list.forEach(data -> {
            Application application = data.toBean(Application.class);
            PPClientContext.applications.add(application);
        });
        noticePaneController.noticeTable.refresh();
        return null;
    }
}
