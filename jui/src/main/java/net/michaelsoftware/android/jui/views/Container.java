package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.Tools;
import net.michaelsoftware.android.jui.listeners.CustomOnClickListener;
import net.michaelsoftware.android.jui.listeners.CustomOnLongClickListener;

import java.util.HashMap;

/**
 * Created by Michael on 05.09.2016.
 */
public class Container extends JuiView {
    private HashMap<Object, Object> value = null;
    private HashMap<Object, Object> properties = null;
    private String click;
    private String longclick;

    public Container(Context activity) {
        super(activity);
    }

    public Container(Context activity, HashMap<Object, Object> hashMap) {
        super(activity);

        if(Tools.isHashmap(hashMap.get("value"))) {
            this.setValue((HashMap<Object, Object>) hashMap.get("value"));

            if (Tools.isString(hashMap.get("click"))) {
                this.setClick((String) hashMap.get("click"));
            }

            if (Tools.isString(hashMap.get("longclick"))) {
                this.setLongClick((String) hashMap.get("longclick"));
            }

            properties = hashMap;
        }
    }

    @Override
    public View getView(JuiParser parser) {
        if(value != null) {
            LinearLayout lin =  parser.parseReturn(value);

            if (!Tools.empty(click)) {
                lin.setOnClickListener(new CustomOnClickListener(parser, click));
            }

            if (!Tools.empty(longclick)) {
                lin.setOnLongClickListener(new CustomOnLongClickListener(parser, longclick));
            }

            return JuiParser.addProperties(lin, properties);
        }

        return null;
    }

    public void setClick(String click) {
        this.click = click;
    }

    public void setLongClick(String longClick) {
        this.longclick = longClick;
    }

    public void setValue(HashMap<Object, Object> value) {
        this.value = value;
    }
}
