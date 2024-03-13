package com.lucifer.pp.client.function;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.lucifer.pp.client.PPClientContext;
import com.lucifer.pp.gui.controller.SearchPaneController;
import com.lucifer.pp.net.data.SearchResponseData;
import com.lucifer.pp.net.netenum.PPProtocolEnum;
import jakarta.annotation.Resource;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import org.springframework.stereotype.Component;


@Component
public class SearchResponseFunction implements PPFunction{

    @Resource
    SearchPaneController searchPaneController;

    private static final PPProtocolEnum protocol = PPProtocolEnum.SEARCH_RESPONSE;

    @Override
    public PPProtocolEnum getProtocol() {
        return protocol;
    }

    @Override
    public Object apply(Object o) {
        PPClientContext.searchData.clear();
        PPClientContext.searchData.addAll(((JSONArray) o).toList(SearchResponseData.class));
        Platform.runLater(()->{
            searchPaneController.searchTable.refresh();
        });
        return null;
    }
}
