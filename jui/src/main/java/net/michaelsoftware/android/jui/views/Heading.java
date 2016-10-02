package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.Tools;

import java.util.HashMap;

/**
 * Created by Michael on 28.08.2016.
 */
public class Heading extends JuiView {
    private HashMap<Object, Object> properties;
    private TextView view;

    public Heading(Context context) {
        super(context);
    }

    public Heading(Context context, HashMap<Object, Object> hashMap) {
        super(context);

        if(Tools.isString(hashMap.get("value"))) {
            this.setValue((String) hashMap.get("value"));

            LinearLayout.LayoutParams view_params = new
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(view_params);

            this.setSize((String) hashMap.get("size"));

            if(Tools.isHashmap(hashMap.get("shadow"))) {
                this.setShadow((HashMap<Object, Object>) hashMap.get("shadow"));
            }
            
            this.properties = hashMap;
        }
    }

    private void setSize(String size) {
        if(!Tools.empty(size) && Tools.isEqual(size, "small")) {
            if (Build.VERSION.SDK_INT < 23) {
                view.setTextAppearance(context, android.R.style.TextAppearance_Medium);
            } else {
                view.setTextAppearance(android.R.style.TextAppearance_Medium);
            }
        } else {
            if (Build.VERSION.SDK_INT < 23) {
                view.setTextAppearance(context, android.R.style.TextAppearance_Large);
            } else {
                view.setTextAppearance(android.R.style.TextAppearance_Large);
            }
        }
    }

    public void setValue(String value) {
        if(view != null && !Tools.empty(value)){
            value = value.replaceAll("&lt;br /&gt;", "<br />").replaceAll("&lt;br/&gt;", "<br />").replaceAll("&lt;br&gt;", "<br />").replaceAll("<br />", "\n");
            value = value.replaceAll("\n ", "\n").replaceAll(" \n", "\n");
            value = value.replaceAll("&lt;", "<").replaceAll("&gt;", ">");

            view.setText(value);
        } else if(!Tools.empty(value)) {
            view = new TextView(context);

            this.setValue(value);
        } else {
            view = new TextView(context);
        }
    }

    @Override
    public View getView(JuiParser parser) {

        return JuiParser.addProperties(view, properties);
    }

    public void setShadow(HashMap<Object, Object> shadow) {

        int color = Color.BLACK;
        if(Tools.isString(shadow.get("color"))) {
            color = Tools.parseColor((String) shadow.get("color"));
        }

        int offsetX = 3;
        if(Tools.isInt(shadow.get("x"))) {
            offsetX = (Integer) shadow.get("x")*2;
        }

        int offsetY = 3;
        if(Tools.isInt(shadow.get("y"))) {
            offsetY = (Integer) shadow.get("y")*2;
        }

        float scale = 1.5f;
        if(Tools.isInt(shadow.get("scale"))) {
            scale = (Integer) shadow.get("scale")*2;
        }

        view.setShadowLayer(scale, offsetX, offsetY, color);
    }
}
