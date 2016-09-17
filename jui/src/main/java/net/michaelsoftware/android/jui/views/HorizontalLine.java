package net.michaelsoftware.android.jui.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import net.michaelsoftware.android.jui.JuiParser;
import net.michaelsoftware.android.jui.Tools;

import java.util.HashMap;

/**
 * Created by Michael on 05.09.2016.
 */
public class HorizontalLine extends JuiView {
    private int color = 0;

    public HorizontalLine(Context activity) {
        super(activity);
    }

    public HorizontalLine(Context activity, HashMap<Object, Object> hashMap) {
        super(activity);

        if(Tools.isString(hashMap.get("color"))) {
            color = Tools.parseColor((String) hashMap.get("color"));
        }
    }

    @Override
    public View getView(JuiParser parser) {

        View ruler = new View(parser.getActivity());

        if(color != 0) {
            ruler.setBackgroundColor(color);
        } else {
            ruler.setBackgroundColor(parser.getConfig().horizontalLineColor);
        }

        ruler.setLayoutParams(new ViewGroup.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, 2));

        return ruler;
    }
}
