package de.michaelsoftware.android.Vision.tools.gui.views;

import android.content.Context;
import android.view.View;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.Tools;
import net.michaelsoftware.android.jui.views.InputColorView;
import net.michaelsoftware.android.jui.views.InputDateView;
import net.michaelsoftware.android.jui.views.JuiView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Michael on 29.08.2016.
 */
public class AutoInput extends JuiView {

    private AutoInputView edt;
    private InputColorView btnColor;
    private InputDateView btnDate;
    private String preset;
    private String name;
    private HashMap<Object, Object> properties;

    public AutoInput(Context context) {
        super(context);
    }

    public AutoInput(Context context, HashMap<Object, Object> hashMap) {
        super(context);


        this.edt = new AutoInputView(context);

        if (Tools.isString(hashMap.get("name"))) {
            this.setName((String) hashMap.get("name"));
        }

        if (Tools.isString(hashMap.get("predefined"))) {
            this.setPredefined((String) hashMap.get("predefined"));
        }

        /* SET VALUE */
        if (Tools.isHashmap(hashMap.get("value"))) {
            this.setValue((HashMap<Object, Object>) hashMap.get("value"));
        }

        /* SET FOCUS */
        if (Tools.isTrue(hashMap.get("focus"))) {
            this.requestFocus();
        }

        properties = hashMap;
    }

    private void setPredefined(String predefined) {

    }

    private void requestFocus() {
        edt.requestFocus();
    }

    @Override
    public View getView(JuiParser parser) {
        if(!Tools.empty(name)) {
            parser.registerSubmitElement(name, edt);

            return parser.addInputProperties( JuiParser.addProperties(edt, properties), properties );
        }

        return null;
    }

    public void setValue(HashMap<Object, Object> values) {
        if(Tools.empty(edt)) {
            this.edt = new AutoInputView(context);
        }

        ArrayList<String> arrayList = new ArrayList<>();
        for(int i = 0, x = values.size(); i < x; i++) {
            if(Tools.isString(values.get(i))) {
                arrayList.add((String) values.get(i));
            }
        }

        this.edt.setValue(arrayList);
    }

    public void setName(String name) {
        this.name = name;
    }
}
