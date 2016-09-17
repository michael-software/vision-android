package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.view.View;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.Tools;
import net.michaelsoftware.android.jui.listeners.CustomOnClickListener;
import net.michaelsoftware.android.jui.listeners.CustomOnLongClickListener;

import java.util.HashMap;

/**
 * Created by Michael on 30.08.2016.
 */
public class Button extends JuiView {

    private android.widget.Button view;

    private String click, longclick;

    private HashMap<Object, Object> properties;

    public Button(Context context, HashMap<Object, Object> hashMap) {
        super(context);

        if (Tools.isString(hashMap.get("value"))) {
            this.setValue((String) hashMap.get("value"));

            if (Tools.isString(hashMap.get("click"))) {
                this.setClick((String) hashMap.get("click"));
            }

            if (Tools.isString(hashMap.get("longclick"))) {
                this.setLongClick((String) hashMap.get("longclick"));
            }

        }

        this.properties = hashMap;
    }

    @Override
    public View getView(JuiParser parser) {

        if (!Tools.empty(click)) {
            view.setOnClickListener(new CustomOnClickListener(parser, click));
        }

        if (!Tools.empty(longclick)) {
            view.setOnLongClickListener(new CustomOnLongClickListener(parser, longclick));
        }

        return JuiParser.addProperties(view, properties);
    }

    public void setValue(String value) {
        if(Tools.isString(value)) {
            if(view == null) {
                view = new android.widget.Button(context);
            }

            this.view.setText(value);
        }
    }

    public void setClick(String click) {
        this.click = click;
    }

    public void setLongClick(String longClick) {
        this.longclick = longClick;
    }
}
