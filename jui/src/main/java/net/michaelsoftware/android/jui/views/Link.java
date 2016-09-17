package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.widget.TextView;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.Tools;
import net.michaelsoftware.android.jui.listeners.CustomOnClickListener;
import net.michaelsoftware.android.jui.listeners.CustomOnLongClickListener;

import java.util.HashMap;

/**
 * Created by Michael on 31.08.2016.
 */
public class Link extends JuiView {
    private final HashMap<Object, Object> properties;
    private String click;
    private String longClick;
    private TextView view;
    private String value;

    public Link(Context context, HashMap<Object, Object> hashMap) {
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
        if (Tools.isString(value)) {
            view.setText(value);

            view.setPaintFlags(view.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            if (!Tools.empty(click)) {
                view.setOnClickListener(new CustomOnClickListener(parser, click));
            }

            if (!Tools.empty(longClick)) {
                view.setOnLongClickListener(new CustomOnLongClickListener(parser, longClick));
            }

            return JuiParser.addProperties(view, properties);
        }
        return null;
    }

    public void setValue(String value) {
        if(Tools.isString(value)) {
            if(view == null) {
                view = new TextView(context);
            }

            this.value = value;
        }
    }

    public void setClick(String click) {
        this.click = click;
    }

    public void setLongClick(String longClick) {
        this.longClick = longClick;
    }
}
