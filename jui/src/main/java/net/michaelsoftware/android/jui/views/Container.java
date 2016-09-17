package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.util.Log;
import android.view.View;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.Tools;

import java.util.HashMap;

/**
 * Created by Michael on 05.09.2016.
 */
public class Container extends JuiView {
    private HashMap<Object, Object> value = null;

    public Container(Context activity) {
        super(activity);
    }

    public Container(Context activity, HashMap<Object, Object> hashMap) {
        super(activity);

        if(Tools.isHashmap(hashMap.get("value"))) {
            this.setValue((HashMap<Object, Object>) hashMap.get("value"));
        }
    }

    @Override
    public View getView(JuiParser parser) {
        if(value != null) {
            return parser.parseReturn(value);
        }

        return null;
    }

    public void setValue(HashMap<Object, Object> value) {
        this.value = value;
    }
}
