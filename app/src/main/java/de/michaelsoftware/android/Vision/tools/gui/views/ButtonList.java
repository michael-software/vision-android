package de.michaelsoftware.android.Vision.tools.gui.views;

import android.content.Context;
import android.view.View;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.Tools;
import net.michaelsoftware.android.jui.views.JuiView;

import java.util.HashMap;

/**
 * Created by Michael on 08.09.2016.
 */
public class ButtonList extends JuiView {
    private HashMap<Object, Object> properties;
    private HashMap<Object, Object> value;

    public ButtonList(Context activity) {
        super(activity);
    }

    public ButtonList(Context activity, HashMap<Object, Object> hashMap) {
        super(activity, hashMap);

        if (Tools.isHashmap(hashMap.get("value"))) {
            this.setValue((HashMap<Object, Object>) hashMap.get("value"));

            this.properties = hashMap;
        }
    }

    @Override
    public View getView(JuiParser parser) {
        if(this.value != null) {
            View buttonList = new ButtonListView(parser, this.properties);

            return JuiParser.addProperties(buttonList, this.properties);
        }

        return null;
    }

    public void setValue(HashMap<Object, Object> value) {
        this.value = value;
    }
}
