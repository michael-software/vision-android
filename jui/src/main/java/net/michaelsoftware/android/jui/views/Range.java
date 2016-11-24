package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.view.View;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.Tools;

import java.util.HashMap;

/**
 * Created by Michael on 05.09.2016.
 */
public class Range extends JuiView {
    private String name;
    private int value = 0;
    private int min = 0;
    private int max = 2;
    private HashMap<Object, Object> properties;

    public Range(Context activity) {
        super(activity);
    }

    public Range(Context activity, HashMap<Object, Object> hashMap) {
        super(activity, hashMap);

        if (Tools.isString(hashMap.get("name"))) {
            this.setName((String) hashMap.get("name"));

            if (Tools.isInt(hashMap.get("value"))) {
                this.setValue((Integer) hashMap.get("value"));
            }

            if (Tools.isInt(hashMap.get("min"))) {
                this.setMin((Integer) hashMap.get("min"));
            }

            if (Tools.isInt(hashMap.get("max"))) {
                this.setMax((Integer) hashMap.get("max"));
            }

            this.properties = hashMap;
        }
    }

    @Override
    public View getView(JuiParser parser) {
        if(!Tools.empty(name)) {
            View range = new RangeView(parser.getActivity());
            ((RangeView) range).setMin(min);
            ((RangeView) range).setMax(max);
            ((RangeView) range).setProgress(value);
            ((RangeView) range).setSwipeRefreshLayout(parser.getSwipeRefreshLayout());

            parser.registerSubmitElement(name, range);

            range = JuiParser.addProperties(range, properties);

            return parser.addInputProperties(range, properties);
        }

        return null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public void setMax(int max) {
        this.max = max;
    }
}
