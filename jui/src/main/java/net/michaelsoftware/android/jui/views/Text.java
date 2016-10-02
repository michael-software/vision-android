package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
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
public class Text extends JuiView {
    private HashMap<Object, Object> properties;
    private TextView view;

    public Text(Context context) {
        super(context);
    }

    public Text(Context context, HashMap<Object, Object> hashMap) {
        super(context);

        if(Tools.isString(hashMap.get("value"))) {
            this.setValue((String) hashMap.get("value"));

            LinearLayout.LayoutParams view_params = new
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(view_params);


            if(Tools.isString(hashMap.get("align"))) {
                String valueAlign = (String) hashMap.get("align");

                this.setAlignment(valueAlign);
            }

            if(Tools.isString(hashMap.get("appearance"))) {
                String appearance = (String) hashMap.get("appearance");

                this.setAppearance(appearance);
            }

            if(Tools.isHashmap(hashMap.get("shadow"))) {
                this.setShadow((HashMap<Object, Object>) hashMap.get("shadow"));
            }

            this.properties = hashMap;
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
        }
    }

    public void setAlignment(String alignment) {
        if(view != null) {
            if (Tools.isEqual(alignment, "center")) {
                view.setGravity(Gravity.CENTER);
            } else if (Tools.isEqual(alignment, "right")) {
                view.setGravity(Gravity.END);
            } else if (Tools.isEqual(alignment, "left")) {
                view.setGravity(Gravity.START);
            }
        }
    }

    public void setAppearance(String appearance) {
        if(view != null) {
            if (Tools.contains(appearance, "bold")) {
                view.setTypeface(null, Typeface.BOLD);
            }

            if (Tools.contains(appearance, "italic")) {
                view.setTypeface(null, Typeface.ITALIC);
            }

            if (Tools.contains(appearance, "underline")) {
                view.setPaintFlags(view.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }

            if (Tools.contains(appearance, "strikethru")) {
                view.setPaintFlags(view.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
        }
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

    @Override
    public View getView(JuiParser parser) {
        return JuiParser.addProperties(view, properties);
    }
}
