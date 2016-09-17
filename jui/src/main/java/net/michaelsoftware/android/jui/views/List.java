package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.view.View;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.Tools;

import java.util.HashMap;

/**
 * Created by Michael on 05.09.2016.
 */
public class List extends JuiView {
    private HashMap<Object, Object> value = null;
    private HashMap<Object, Object> click = new HashMap<>();
    private HashMap<Object, Object> longClick = new HashMap<>();
    private HashMap<Object, Object> properties;

    public List(Context activity) {
        super(activity);
    }

    public List(Context activity, HashMap<Object, Object> hashMap) {
        super(activity);

        if(Tools.isHashmap(hashMap.get("value"))) {
            this.value = (HashMap<Object, Object>) hashMap.get("value");

            if(Tools.isHashmap(hashMap.get("click"))) {
                this.click = (HashMap<Object, Object>) hashMap.get("click");
            }

            if(Tools.isHashmap(hashMap.get("longclick"))) {
                this.longClick = (HashMap<Object, Object>) hashMap.get("longclick");
            }

            this.properties = hashMap;
        }
    }

    @Override
    public View getView(JuiParser parser) {

        if(!Tools.empty(value)) {

            ListView lv = new ListView(parser.getActivity());

            for(int i = 0, x = value.size(); i < x; i++) {
                if(Tools.isString(value.get(i))) {
                    String elemValue = (String) value.get(i);

                    if(Tools.isString(click.get(i)) && Tools.isString(longClick.get(i))) {
                        lv.addItem(parser, elemValue, (String) click.get(i), (String) longClick.get(i));
                    } else if(Tools.isString(click.get(i))) {
                        lv.addItem(parser, elemValue, (String) click.get(i));
                    } else if(Tools.isString(longClick.get(i))) {
                        lv.addItem(parser, elemValue, null, (String) longClick.get(i));
                    } else {
                        lv.addItem(parser, elemValue);
                    }
                }
            }

            return JuiParser.addProperties(lv, this.properties);
        }

        return null;
    }
}
