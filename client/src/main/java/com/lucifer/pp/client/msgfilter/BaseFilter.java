package com.lucifer.pp.client.msgfilter;

import com.lucifer.pp.gui.constant.GUIConstant;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class BaseFilter implements MessageFilter{
    @Override
    public String encode(String html) {
        StringBuilder result = new StringBuilder();
        Document doc = Jsoup.parse(html);
        Element p = doc.body().getElementsByTag("p").first();
        assert p != null;
        p.childNodes().forEach(node -> {
            if (node instanceof Element element){
                if (element.nodeName().equals("span")){
                    result.append(element.text());
                }else if (element.nodeName().equals("img")){
                    String s = element.attributes().get(GUIConstant.EMOJI_IMAGE_ATTR_KEY);
                    result.append("[[emoji:").append(s).append("]]");
                }
            }else if (node instanceof TextNode){
                result.append(((TextNode) node).text());
            }
        });
        return result.toString();
    }

    @Override
    public String decode(String s){
        return s;
    }
}
