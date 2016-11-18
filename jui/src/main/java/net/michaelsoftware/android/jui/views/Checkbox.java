package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.Tools;

import java.util.HashMap;

/**
 * Created by Michael on 05.09.2016.
 */
public class Checkbox extends JuiView {
    private String name = null;
    private HashMap<Object, Object> properties;
    private boolean checked = false;

    public Checkbox(Context activity) {
        super(activity);
    }

    public Checkbox(Context activity, HashMap<Object, Object> hashMap) {
        super(activity);

        if (Tools.isString(hashMap.get("name"))) {
            this.setName((String) hashMap.get("name"));

            if (!Tools.empty(hashMap.get("checked"))) {
                if (Tools.isBool(hashMap.get("checked")) && ((boolean) hashMap.get("checked"))) {
                    this.setChecked(true);
                }
            }

            this.properties = hashMap;
        }
    }

    @Override
    public View getView(JuiParser parser) {
        if(!Tools.empty(name)) {
            View checkbox = new CheckBox(parser.getActivity());

            if(this.checked) {
                ((CheckBox) checkbox).setChecked(true);
            }

            parser.registerSubmitElement(name, checkbox);
            checkbox = JuiParser.addProperties(checkbox, this.properties);

            return parser.addInputProperties(checkbox, properties);
        }

        return null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
