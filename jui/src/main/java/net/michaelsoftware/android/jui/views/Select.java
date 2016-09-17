package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.Tools;
import net.michaelsoftware.android.jui.models.NameValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Michael on 28.08.2016.
 */
public class Select extends JuiView {
    private HashMap<Object, Object> properties;
    private SpinnerView spinnerView;
    private String name;

    public Select(Context context) {
        super(context);
    }

    public Select(Context context, HashMap<Object, Object> hashMap) {
        super(context);

        if (Tools.isHashmap(hashMap.get("value")) && Tools.isString(hashMap.get("name"))) {
            this.setValue((HashMap<Object, Object>) hashMap.get("value"));
            this.setName((String) hashMap.get("name"));


            this.properties = hashMap;
        }
    }

    public void setValue(HashMap<Object, Object> value) {
        if(!Tools.empty(value)){

            if(spinnerView == null) {
                spinnerView = new SpinnerView(context);
            }

            spinnerView.setValue(value);

        }
    }

    @Override
    public View getView(JuiParser parser) {

        if(Tools.isString(name)) {
            parser.registerSubmitElement(name, spinnerView);

            View view = parser.addInputProperties(spinnerView, properties);

            return JuiParser.addProperties(view, properties);
        }

        return null;
    }

    public void setName(String name) {
        this.name = name;
    }
}
