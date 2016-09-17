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

            if(!Tools.isString(hashMap.get("size"))) {
                this.setSize((String) hashMap.get("size"));
            }
            
            this.properties = hashMap;
        }
    }

    private void setSize(String size) {
        if(Tools.empty(size) || Tools.isEqual(size, "small")) {
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
        }
    }

    @Override
    public View getView(JuiParser parser) {

        return JuiParser.addProperties(view, properties);
    }
}
