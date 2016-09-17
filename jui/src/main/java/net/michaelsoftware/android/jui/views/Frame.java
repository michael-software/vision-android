package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.view.View;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.Tools;

import java.util.HashMap;

/**
 * Created by Michael on 06.09.2016.
 */
public class Frame extends JuiView {
    private String src;
    private String html;
    private HashMap<Object, Object> properties;

    public Frame(Context activity) {
        super(activity);
    }

    public Frame(Context activity, HashMap<Object, Object> hashMap) {
        super(activity, hashMap);

        if (Tools.isString(hashMap.get("value"))) {
            this.setSrc((String) hashMap.get("value"));

            this.properties = hashMap;
        } else if (Tools.isString(hashMap.get("html"))) {
            this.setHtml((String) hashMap.get("html"));

            this.properties = hashMap;
        }
    }

    @Override
    public View getView(JuiParser parser) {
        if(!Tools.empty(this.src)) {
            View frameView = new FrameView(parser, false);
            ((FrameView) frameView).setUrl(this.src);

            return JuiParser.addProperties(frameView, properties);
        } else if(!Tools.empty(this.html)) {
            View frameView = new FrameView(parser, false);
            ((FrameView) frameView).setHtml(this.html);

            return JuiParser.addProperties(frameView, properties);
        }

        return null;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public void setHtml(String html) {
        this.html = html;
    }
}
